package tpi.tusi.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.ui.adapters.MisCursosAdapter
import tpi.tusi.ui.interfaces.FiltrosListener
import tpi.tusi.ui.utils.CursoUtils
import tpi.tusi.ui.utils.CursoUtils.cargarCursos

/**
 * A simple [Fragment] subclass.
 * Use the [ListarMisCursosEstudianteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListarMisCursosEstudianteFragment : Fragment(), FiltrosListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var  recyclerView: RecyclerView
    private lateinit var misCursosAdapter: MisCursosAdapter
    private lateinit var spinnerCategorias: Spinner
    private val cursoDaoDos= CursosDao()
    private val etiquetasDao = EtiquetasDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listar_mis_cursos_estudiante, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)

        recyclerView = view.findViewById(R.id.recyclerView_miscursos_Estudiante) // Cambiado el ID correcto
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        // Cargo cursos usando corrutinas
        cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            idUsuario= idUsuario,
            fkEstadoCurso = 1
        )

        return view
    }
    override fun onResume() {
        super.onResume()
        // Recarga los cursos cada vez que el fragmento es visible
        cargarMisCursos()
    }
    private fun cargarMisCursos() {
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            idUsuario = idUsuario,
            fkEstadoCurso = 1
        )
    }
    override fun onBuscarClicked(nombre: String?, fkEtiqueta: Long?, fkEstadoCurso: Int?) {
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            idUsuario= idUsuario,
            nombre = nombre,
            fkEtiqueta = fkEtiqueta?.toInt(),
            fkEstadoCurso = 1,//Siempre el estado del curso sera 1 que son los Aceptados
        )
    }

}