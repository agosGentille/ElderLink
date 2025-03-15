package tpi.tusi.ui.fragments

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
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

class ListarCursosAdminFragment : Fragment(), FiltrosListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cursosAdapter: CursosAdapter
    private val cursoDaoDos = CursosDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Se infla el layout de cursos para admin.
        val view = inflater.inflate(R.layout.fragment_listar_cursos_admin, container, false)

        recyclerView = view.findViewById(R.id.recyclerView_cursos_admin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val nombre: String? = null
        val fkEtiqueta: Int? = null
        val fkEstadoCurso: Int? = null
        val usuario: Int? = null
        // Cargar los cursos inicialmente
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results_admin),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta,
            fkEstadoCurso = fkEstadoCurso,//no hay restriccion en cuanto a Estados Puede verlos todos
            usuario = usuario//El admin no debe pasar el usuario va en null ya que ve los cursos de todos los gestores
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
            fkEstadoCurso = fkEstadoCurso,
            usuario = null
        )
    }
    fun recargarCursos(){//Esto lo dejo aqui porque vi en una fucion de leo un llamado al cargar cursos antiguo
        val nombre: String? = null
        val fkEtiqueta: Int? = null
        val fkEstadoCurso: Int? = null
        val usuario: Int? = null
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results_admin),
            nombre = nombre,
            fkEtiqueta = fkEtiqueta,
            fkEstadoCurso = fkEstadoCurso,//no hay restriccion en cuanto a Estados Puede verlos todos
            usuario = usuario//El admin no debe pasar el usuario va en null ya que ve los cursos de todos los gestores
        )
    }

    private fun formatDate(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.getDefault())
        return formatter.format(instant)
    }
}
