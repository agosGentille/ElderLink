package tpi.tusi.ui.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.adapters.CursosAdapter
import tpi.tusi.ui.adapters.MisCursosAdapter

object CursoUtils {
    fun cargarCursos(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        cursoDaoDos: CursosDao,
        recyclerView: RecyclerView,
        noResultsTextView: TextView?,
        idUsuario: Int? = null,
        nombre: String? = null,
        fkEtiqueta: Int? = null,
        fkEstadoCurso: Int? = null,
        usuario: Int? = null
    ) {
        fun recargarCursos() {
            lifecycleScope.launch {
                try {
                    val cursosList = cursoDaoDos.obtenerCursos(
                        idUsuario = idUsuario,
                        nombre = nombre,
                        fkEtiqueta = fkEtiqueta,
                        fkEstadoCurso = fkEstadoCurso,
                        usuario = usuario
                    )

                    val sharedPreferences = context.getSharedPreferences("usuario", Context.MODE_PRIVATE)
                    val rolesSet = sharedPreferences.getStringSet("roles", setOf()) ?: setOf()
                    val userRole = rolesSet.firstOrNull() ?: "estudiante"

                    if (cursosList.isNotEmpty()) {
                        noResultsTextView?.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        if (usuario != null || idUsuario != null) {
                            // Pasamos la función de recarga como callback
                            val cursosAdapter = MisCursosAdapter(cursosList, userRole) { recargarCursos() }
                            recyclerView.adapter = cursosAdapter
                        } else {
                            recyclerView.adapter = CursosAdapter(cursosList, userRole)
                        }
                    } else {
                        noResultsTextView?.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        recyclerView.adapter = CursosAdapter(emptyList(), userRole)
                    }
                } catch (e: Exception) {
                    Log.e("ListarCursos", "Error al cargar los cursos", e)
                }
            }
        }

        // Llamada inicial para cargar los cursos
        recargarCursos()
    }
}