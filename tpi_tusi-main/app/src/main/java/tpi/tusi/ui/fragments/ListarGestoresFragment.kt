package tpi.tusi.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.UsuariosDao
import android.graphics.Color
import android.view.Gravity.CENTER
import android.widget.RadioButton
import android.widget.RadioGroup
import tpi.tusi.ui.activities.GestionEstadoCursoActivity
import tpi.tusi.ui.activities.GestionEstadoGestorActivity
import tpi.tusi.ui.dto.UsuarioConEstado

class ListarGestoresFragment : Fragment() {

    private lateinit var tablaGestores: TableLayout
    private lateinit var etBuscar: TextView
    private lateinit var btnBuscar: Button
    private lateinit var btnBorrarFiltros: Button
    private lateinit var rgEstados: RadioGroup
    private var rgSeleccionado: RadioButton? = null

    private val usuariosDao = UsuariosDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listar_gestores, container, false)

        tablaGestores = view.findViewById(R.id.tablaGestores)
        etBuscar = view.findViewById(R.id.EditText_buscar)
        btnBuscar = view.findViewById(R.id.button_buscar)
        btnBorrarFiltros = view.findViewById(R.id.button_eliminar_filtros)
        rgEstados = view.findViewById(R.id.rgStatus)

        cargarUsuarios()

        btnBuscar.setOnClickListener {
            buscar(etBuscar.text.toString())
        }

        btnBorrarFiltros.setOnClickListener {
            cargarUsuarios()
            rgEstados.clearCheck()
        }

        return view
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch {
            val usuarios = usuariosDao.obtenerGestores()
            cargarUsuariosConFiltro(usuarios)
        }
    }

    private fun cargarUsuariosConFiltro(usuarios: List<UsuarioConEstado>) {
        tablaGestores.removeAllViews()
        agregarFilaCabecera()

        for (usuario in usuarios) {
            val tableRow = crearFilaUsuario(usuario)
            tablaGestores.addView(tableRow)
        }
    }

    private fun agregarFilaCabecera() {
        val headerRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#303F9F"))
        }

        val headerEstadoTextView = TextView(requireContext()).apply {
            text = ""
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = CENTER
        }

        val headerNombreTextView = TextView(requireContext()).apply {
            text = "Nombre"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = CENTER
        }

        val headerApellidoTextView = TextView(requireContext()).apply {
            text = "Apellido"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = CENTER
        }

        val headerUsuarioTextView = TextView(requireContext()).apply {
            text = "Usuario"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = CENTER
        }

        val headerVerTextView = TextView(requireContext()).apply {
            text = "Ver"
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setPadding(8, 8, 8, 8)
            setTextColor(Color.WHITE)
            gravity = CENTER
        }
        headerRow.addView(headerEstadoTextView)
        headerRow.addView(headerNombreTextView)
        headerRow.addView(headerApellidoTextView)
        headerRow.addView(headerUsuarioTextView)
        headerRow.addView(headerVerTextView)

        tablaGestores.addView(headerRow)
    }

    private fun crearFilaUsuario(usuario: UsuarioConEstado): TableRow {
        return TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            val estadoView = View(requireContext()).apply {
                val tamañoAncho = (15f * resources.displayMetrics.density).toInt()
                val tamañoAlto = (30f * resources.displayMetrics.density).toInt()

                layoutParams = TableRow.LayoutParams(tamañoAncho, tamañoAlto)
                setBackgroundColor(
                    when (usuario.estado) {
                        1 -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                        2 -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                        3 -> ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                        else -> ContextCompat.getColor(requireContext(), android.R.color.transparent)
                    }
                )
            }

            val nombreTextView = TextView(requireContext()).apply {
                text = usuario.nombre
                setPadding(8, 8, 8, 8)
                textSize = 16f
                gravity = CENTER
            }

            val apellidoTextView = TextView(requireContext()).apply {
                text = usuario.apellido
                setPadding(8, 8, 8, 8)
                textSize = 16f
                gravity = CENTER
            }

            val nombreUsuarioTextView = TextView(requireContext()).apply {
                text = usuario.nombreUsuario
                setPadding(8, 8, 8, 8)
                textSize = 16f
                gravity = CENTER
            }

            val verButton = ImageButton(requireContext()).apply {
                setImageResource(R.drawable.ic_ver) // Ícono de "ver"
                setPadding(8, 8, 8, 8)
                contentDescription = "Ver usuario"

                setOnClickListener {
                    verUsuario(usuario.id_usuario)
                }
            }

            // Agregar las celdas a la fila
            addView(estadoView)
            addView(nombreTextView)
            addView(apellidoTextView)
            addView(nombreUsuarioTextView)
            addView(verButton)
        }
    }

    private fun buscar(nombre: String) {
        lifecycleScope.launch {
            if (rgEstados.checkedRadioButtonId == -1)
                cargarUsuariosConFiltro(usuariosDao.obtenerGestoresFiltrados(nombre))
            else {
                rgSeleccionado = view?.findViewById(rgEstados.checkedRadioButtonId)
                cargarUsuariosConFiltro(usuariosDao.obtenerGestoresFiltrados(nombre, rgSeleccionado!!.tag.toString().toInt()))
            }
        }
    }

    private fun verUsuario(id_usuario: Long) {
        val intent = Intent(requireActivity(), GestionEstadoGestorActivity::class.java)
        intent.putExtra("id", id_usuario)
        startActivity(intent)
    }
}
