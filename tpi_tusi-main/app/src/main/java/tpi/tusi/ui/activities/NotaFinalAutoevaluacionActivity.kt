package tpi.tusi.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.AutoevaluacionesDao
import tpi.tusi.ui.entities.NotasUsuarios

class NotaFinalAutoevaluacionActivity : BaseActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var btnVolver: Button

    private var autoevaluacionesDao = AutoevaluacionesDao()

    private var autoevalId: Long = 0L
    private var usuarioId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nota_final_autoevaluacion)

        autoevalId = intent.getLongExtra("autoevalId", 0L)
        usuarioId = intent.getLongExtra("usuarioId", 0L)

        tableLayout = findViewById(R.id.tablaIntentos)
        btnVolver = findViewById(R.id.button_volver)

        btnVolver.setOnClickListener {
            val intent = Intent(this@NotaFinalAutoevaluacionActivity, MainEstudianteActivity::class.java)
            startActivity(intent)
        }

        cargarIntentosDesdeBaseDeDatos()
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_nota_final_autoevaluacion
    }

    private fun cargarIntentosDesdeBaseDeDatos() {
        lifecycleScope.launch {
            val listaIntentos = autoevaluacionesDao.obtenerIntentos(autoevalId, usuarioId) // Función que consulta la base de datos

            listaIntentos.forEach { intento ->
                agregarFila(intento)
            }
        }
    }

    private fun agregarFila(intento: NotasUsuarios) {
        val tableRow = TableRow(this)

        val intentoTextView = TextView(this).apply {
            text = intento.nro_intento.toString()
            setTextColor(resources.getColor(android.R.color.black))
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.table_border)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val notaFinalTextView = TextView(this).apply {
            text = intento.calificacion.toString()
            setTextColor(resources.getColor(android.R.color.black))
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.table_border)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val fechaTextView = TextView(this).apply {
            text = intento.fecha.toString()
            setTextColor(resources.getColor(android.R.color.black))
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.table_border)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Agrega los TextView al TableRow
        tableRow.addView(intentoTextView)
        tableRow.addView(notaFinalTextView)
        tableRow.addView(fechaTextView)

        // Finalmente, agrega el TableRow al TableLayout
        tableLayout.addView(tableRow)
    }


}