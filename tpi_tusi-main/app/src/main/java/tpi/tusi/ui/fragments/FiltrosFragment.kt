// FiltrosFragment.kt
package tpi.tusi.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.ui.entities.Etiquetas
import tpi.tusi.ui.interfaces.FiltrosListener

class FiltrosFragment : Fragment() {
    private var filtrosListener: FiltrosListener? = null
    private lateinit var editTextBuscar: EditText
    private lateinit var spinnerCategorias: Spinner
    private val etiquetasDao = EtiquetasDao()
    private lateinit var checkboxPendientes: CheckBox
    private lateinit var checkboxAceptados: CheckBox
    private lateinit var checkboxCancelados: CheckBox
    private var etiquetas: List<Etiquetas> = emptyList()
    private lateinit var buttonBuscar: Button

    private lateinit var sp: SharedPreferences
    private var usuarioId: Long? = 0L
    private var rolesUsuario: MutableSet<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filtros, container, false)

        editTextBuscar = view.findViewById(R.id.EditText_buscar)
        spinnerCategorias= view.findViewById(R.id.spinner_categorias)
        checkboxPendientes = view.findViewById(R.id.checkbox_pendientes)
        checkboxAceptados = view.findViewById(R.id.checkbox_aceptados)
        checkboxCancelados = view.findViewById(R.id.checkbox_cancelados)

        sp = requireContext().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        usuarioId = sp.getLong("id", 0L)
        rolesUsuario = sp.getStringSet("roles", null)

        if(rolesUsuario!!.contains("admin")){
            mostrarFiltros()
        }else{
            ocultarFiltros()
        }

        configurarCheckboxes()
        cargarEtiquetas()
        buttonBuscar = view.findViewById(R.id.button_buscar)

        buttonBuscar.setOnClickListener {
            val textoBusqueda = editTextBuscar.text.toString().trim()
            val etiquetaSeleccionada = obtenerEtiquetaSeleccionada()
            val estadoSeleccionado = obtenerEstadoSeleccionado()

            Log.d("FiltrosFragment", "Texto búsqueda: $textoBusqueda")
            Log.d("FiltrosFragment", "Etiqueta seleccionada: $etiquetaSeleccionada")
            Log.d("FiltrosFragment", "Estado seleccionado: $estadoSeleccionado")

            Log.d("FiltrosFragment", "Texto búsqueda: $textoBusqueda")
            Log.d("FiltrosFragment", "Etiqueta seleccionada: $etiquetaSeleccionada")

            filtrosListener?.onBuscarClicked(
                nombre = if (textoBusqueda.isEmpty()) null else textoBusqueda,
                fkEtiqueta = etiquetaSeleccionada,
                fkEstadoCurso = estadoSeleccionado
            )
        }

        return view
    }
    private fun configurarCheckboxes() {
        val checkboxes = listOf(checkboxPendientes, checkboxAceptados, checkboxCancelados)

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Desmarcar los otros checkboxes
                    checkboxes.forEach { otherCheckbox ->
                        if (otherCheckbox != buttonView) {
                            otherCheckbox.isChecked = false
                        }
                    }
                }
            }
        }
    }
    private fun obtenerEstadoSeleccionado(): Int? {
        return when {
            checkboxAceptados.isChecked -> 1
            checkboxCancelados.isChecked -> 2
            checkboxPendientes.isChecked -> 3
            else -> null
        }
    }

    private fun cargarEtiquetas() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                etiquetas = etiquetasDao.obtenerEtiquetas()

                // Crear lista de opciones para el spinner
                val opciones = mutableListOf<String>()
                opciones.add("Seleccione una categoría") // Primera opción por defecto
                opciones.addAll(etiquetas.map { it.nombre })

                // Crear y configurar el adapter
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    opciones
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                // Asignar el adapter al spinner
                spinnerCategorias.adapter = adapter

                Log.d("FiltrosFragment", "Etiquetas cargadas: ${etiquetas.size}")
            } catch (e: Exception) {
                Log.e("FiltrosFragment", "Error al cargar etiquetas", e)
                // Aquí podrías mostrar un mensaje de error al usuario
            }
        }
    }
    private fun obtenerEtiquetaSeleccionada(): Long? {
        val posicion = spinnerCategorias.selectedItemPosition
        // Si la posición es 0, significa que está seleccionada la opción "Seleccione una categoría"
        return if (posicion > 0) {
            // Restamos 1 a la posición porque la primera opción es "Seleccione una categoría"
            etiquetas[posicion - 1].id_etiqueta
        } else {
            null
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Buscar el parent fragment
        val parentFragment = parentFragment
        if (parentFragment is FiltrosListener) {
            filtrosListener = parentFragment
        } else {
            Log.e("FiltrosFragment", "Error: Parent fragment no implementa FiltrosListener")
        }
    }

    fun ocultarFiltros(){
        checkboxAceptados.visibility = View.GONE
        checkboxCancelados.visibility = View.GONE
        checkboxPendientes.visibility = View.GONE
    }

    fun mostrarFiltros(){
        checkboxAceptados.visibility = View.VISIBLE
        checkboxCancelados.visibility = View.VISIBLE
        checkboxPendientes.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        filtrosListener = null
    }
}

