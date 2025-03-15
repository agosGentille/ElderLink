package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.EventosDao
import tpi.tusi.ui.adapters.EventosAdapter

class ListadoEventosActivity : BaseActivity() {

    private lateinit var rvEventos: RecyclerView
    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnEliminarFiltro: Button
    private lateinit var btnAgregarEvento: ImageButton
    private lateinit var sp: SharedPreferences
    private var usuarioId: Long? = 0L
    private var rolesUsuario: MutableSet<String>? = null
    var rol = 0

    private val eventosDao = EventosDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rvEventos = findViewById(R.id.recyclerView_eventos)
        etBuscar = findViewById(R.id.EditText_buscar)
        btnBuscar = findViewById(R.id.button_buscar)
        btnEliminarFiltro = findViewById(R.id.button_eliminar_filtros)
        btnAgregarEvento = findViewById(R.id.btnAgregarEvento)

        sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
        usuarioId = sp.getLong("id", 0L)
        rolesUsuario = sp.getStringSet("roles", null)

        if(rolesUsuario!!.contains("admin")){
            rol = 1
        }else{
            rol = 2
        }

        if(rol != 1){
            btnAgregarEvento.visibility = View.GONE
        }else{
            btnAgregarEvento.visibility = View.VISIBLE
        }

        btnBuscar.setOnClickListener {
            obtenerEventosFiltro(etBuscar.text.toString())
        }

        btnEliminarFiltro.setOnClickListener {
            obtenerEventos()
        }

        btnAgregarEvento.setOnClickListener {
            val intent = Intent(this, AgregarModificarEventoActivity::class.java)
            startActivity(intent)
        }

        rvEventos.layoutManager = LinearLayoutManager(this@ListadoEventosActivity)

        obtenerEventos()

    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_listado_eventos
    }

    private fun obtenerEventos(){

        lifecycleScope.launch {

            val listaEventos = eventosDao.obtenerEventos()

            val eventosAdapter = EventosAdapter(listaEventos, rol, this@ListadoEventosActivity)
            rvEventos.adapter = eventosAdapter

        }

    }

    private fun obtenerEventosFiltro(titulo: String){
        lifecycleScope.launch {

            val listaEventos = eventosDao.obtenerEventosTitulo(titulo)

            val eventosAdapter = EventosAdapter(listaEventos, rol, this@ListadoEventosActivity)
            rvEventos.adapter = eventosAdapter

        }
    }

}