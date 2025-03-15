package tpi.tusi.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.dao.CursoDAO
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.DenunciasDao
import tpi.tusi.ui.adapters.DenunciasAdapter
import tpi.tusi.ui.entities.Cursos
import tpi.tusi.ui.entities.Denuncias
import tpi.tusi.ui.entities.Usuarios

class DenunciasActivity : BaseActivity() {

    private val dDao = DenunciasDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cargarExpandableListView()
    }

    private fun cargarExpandableListView() {
        lifecycleScope.launch {
            val denunciasConDetalles = dDao.obtenerDenunciasConDetalles()

            val denunciasPorCurso = denunciasConDetalles.groupBy { Pair(it.fk_curso_id, it.fk_curso_titulo) }

            val allDenuncias = denunciasPorCurso.map { (cursoInfo, denuncias) ->
                Triple(cursoInfo.first, cursoInfo.second, denuncias)
            }

            val adapter = DenunciasAdapter(this@DenunciasActivity, allDenuncias.toMutableList())
            val expandableListView = findViewById<ExpandableListView>(R.id.elvDenuncias)
            expandableListView.setAdapter(adapter)
        }
    }


    override fun obtenerPantalla(): Int {
        return R.layout.activity_denuncias
    }
}