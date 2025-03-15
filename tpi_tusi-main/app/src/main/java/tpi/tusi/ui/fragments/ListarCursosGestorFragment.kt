package tpi.tusi.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.adapters.CursosAdapter
import tpi.tusi.ui.interfaces.FiltrosListener
import tpi.tusi.ui.utils.CursoUtils
import tpi.tusi.ui.utils.CursoUtils.cargarCursos


class ListarCursosGestorFragment : Fragment(), FiltrosListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var cursosAdapter: CursosAdapter
    private val cursoDaoDos = CursosDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listar_cursos_gestor, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.recyclerView_cursos_gestor)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val nombre: String? = null
        val fkEtiqueta: Int? = null
        val fkEstadoCurso: Int? = null
        val usuario: Int? = null

        //Cargo los Cursos usando corrutinas
        cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta,
            fkEstadoCurso = 1,//para que el gestor no pueda ver cursos de otros estados
            usuario = usuario
        )
        return view
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
            fkEstadoCurso = 1,
            usuario = null
        )
    }

}