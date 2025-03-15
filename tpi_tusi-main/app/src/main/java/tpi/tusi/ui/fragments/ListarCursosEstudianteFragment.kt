package tpi.tusi.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpi.tusi.R
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.lifecycle.lifecycleScope
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.interfaces.FiltrosListener
import tpi.tusi.ui.utils.CursoUtils
import tpi.tusi.ui.utils.CursoUtils.cargarCursos


class ListarCursosEstudianteFragment : Fragment(), FiltrosListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private val cursoDaoDos = CursosDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listar_cursos_estudiante, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.recyclerView_cursos_estudiante)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val nombre: String? = null
        val fkEtiqueta: Int? = null
        val fkEstadoCurso: Int? = 1//Estado de cursos para estudiantes simpre sera 1
        val usuario: Int? = null
        // Cargar los cursos usando corrutinas
        cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta,
            fkEstadoCurso = fkEstadoCurso,
            usuario = usuario
        )

        return view
    }
    override fun onResume() {
        super.onResume()
        // Recarga los cursos cada vez que el fragmento es visible
        cargarMisCursos()
    }
    private fun cargarMisCursos() {
        val nombre: String? = null
        val fkEtiqueta: Int? = null
        val fkEstadoCurso: Int? = 1//Estado de cursos para estudiantes simpre sera 1
        val usuario: Int? = null
        cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta,
            fkEstadoCurso = fkEstadoCurso,
            usuario = usuario
        )
    }
    override fun onBuscarClicked(nombre: String?, fkEtiqueta: Long?, fkEstadoCurso: Int?) {
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta?.toInt(),
            fkEstadoCurso = 1,//Siempre el estado del curso sera 1 que son los Aceptados
            usuario = null //
        )
    }

    private fun formatDate(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.getDefault())
        return formatter.format(instant)
    }
}

