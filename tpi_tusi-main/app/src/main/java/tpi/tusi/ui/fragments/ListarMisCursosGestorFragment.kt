package tpi.tusi.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.activities.CrearCursosActivity
import tpi.tusi.ui.adapters.MisCursosAdapter
import tpi.tusi.ui.interfaces.FiltrosListener
import tpi.tusi.ui.utils.CursoUtils
import tpi.tusi.ui.utils.CursoUtils.cargarCursos


class ListarMisCursosGestorFragment : Fragment(), FiltrosListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var  recyclerView: RecyclerView
    private lateinit var misCursosAdapter: MisCursosAdapter
    private val cursoDaoDos= CursosDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { val view = inflater.inflate(R.layout.fragment_listar_mis_cursos_gestor, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        recyclerView = view.findViewById(R.id.recyclerView_miscursos_gestor)
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
            fkEstadoCurso = null
        )

        // Encuentra el botón para agregar cursos
        val btnAgregarCursos: Button = view.findViewById(R.id.btnAgregarCursos) // Cambia esto por el ID correcto

        btnAgregarCursos.setOnClickListener {
            // Inicia la actividad CrearCursosActivity
            val intent = Intent(requireContext(), CrearCursosActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onBuscarClicked(nombre: String?, fkEtiqueta: Long?, fkEstadoCurso: Int?) {
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        CursoUtils.cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            //idUsuario= idUsuario,
            nombre = nombre,
            fkEtiqueta = fkEtiqueta?.toInt(),
            fkEstadoCurso = fkEstadoCurso,
            usuario = idUsuario
        )
    }


    override fun onResume() {
        super.onResume()
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        cargarCursos(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            cursoDaoDos = cursoDaoDos,
            recyclerView = recyclerView,
            noResultsTextView = view?.findViewById(R.id.textView_no_results),
            fkEstadoCurso = null,
            usuario = idUsuario //importante para que se agreguen los botones en mis cursos
        )
    }
}