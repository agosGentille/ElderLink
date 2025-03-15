package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import tpi.tusi.R
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.data.dao.CursoDAO
import tpi.tusi.data.daos.EtapasDao
import tpi.tusi.ui.adapters.abmEtapasAdapter
import tpi.tusi.ui.entities.Etapas


class AgregarEtapaActivity : BaseActivity() {

    private lateinit var btnFinalizar: Button
    private lateinit var btnAgregarEtapa: ImageButton
    private lateinit var listViewEtapas: ListView

    private var cursoId: Long = 0L
    private lateinit var adapter: abmEtapasAdapter
    private var etapasList = mutableListOf<Etapas>()
    private val cDao = CursoDAO()
    private val eDao = EtapasDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnFinalizar = findViewById(R.id.btnfinalizar)
        btnAgregarEtapa = findViewById(R.id.btnAgregarEtapa)
        listViewEtapas = findViewById(R.id.listViewEtapas)

        cursoId = intent.getLongExtra("cursoId", 0L)
        if (cursoId == 0L) {
            Toast.makeText(this, "Error al recibir el ID del curso", Toast.LENGTH_SHORT).show()
            finish()
        }

        adapter = abmEtapasAdapter(this, etapasList)
        listViewEtapas.adapter = adapter

        cargarEtapas()

        // Botón para guardar cambios
        btnFinalizar.setOnClickListener {
            mensajeConfirmacion()
        }

        // Botón para agregar una nueva instancia de etapa
        btnAgregarEtapa.setOnClickListener {
            adapter.agregarNuevaInstanciaVacia(cursoId)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isKeyboardVisible()) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                } else {
                    mensajeConfirmacion()
                }
            }
        })

    }

    fun isKeyboardVisible(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isAcceptingText
    }


    private fun cargarEtapas() {
        lifecycleScope.launch {
            val etapas = withContext(Dispatchers.IO) {
                eDao.getEtapasPorCurso(cursoId)
            }
            adapter.actualizarEtapas(etapas)
        }
    }

    fun mensajeConfirmacion(){
        AlertDialog.Builder(this)
            .setTitle("Finalizar Proceso de Etapas")
            .setMessage("¿Estás seguro de que deseas finalizar el proceso de ABM de Etapas? Los cambios" +
                    " no guardados se perderán.")
            .setPositiveButton("Sí") { dialog, _ ->
                val intent = Intent(this, MainGestorActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Método para devolver el layout de la actividad
    override fun obtenerPantalla(): Int {
        return R.layout.activity_agregar_etapa
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in etapasList.indices) {
            val etapa = etapasList[i]
            val view = listViewEtapas.getChildAt(i)
            if (view != null) {
                val viewHolder = view.tag as abmEtapasAdapter.ViewHolder
                etapa.titulo = viewHolder.txtTitulo.text.toString()
                etapa.contenido = viewHolder.edtDescripcion.text.toString()
            }
        }
        outState.putParcelableArrayList("etapasList", ArrayList(etapasList))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredEtapas = savedInstanceState.getParcelableArrayList<Etapas>("etapasList")
        if (restoredEtapas != null) {
            etapasList.clear()
            etapasList.addAll(restoredEtapas)
            adapter.notifyDataSetChanged()
        }
    }

}