package tpi.tusi.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.activities.AgregarAutoevaluacionActivity
import tpi.tusi.ui.activities.AgregarEtapaActivity
import tpi.tusi.ui.activities.EditarCursosActivity
import tpi.tusi.ui.activities.vistaEspecificaCurso
import tpi.tusi.ui.entities.Cursos

class MisCursosAdapter(
    private val cursosList: List<Cursos>,
    private val userRole: String,
    private val onCursoRemoved: () -> Unit
) : RecyclerView.Adapter<MisCursosAdapter.CursoViewHolder>() {

    private val cursosDao = CursosDao()
    class CursoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.textView_titulo_curso)
        val descripcionTextView: TextView = view.findViewById(R.id.textView_descripcion_curso)
        val imagenPerfil: ImageView = view.findViewById(R.id.image_perfil) // Añade esta línea para obtener la referencia al ImageView
        val buttonContainer: ViewGroup = view.findViewById(R.id.button_container) // Un contenedor para los botones
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        // Obtener idUsuario desde SharedPreferences
        val sharedPreferences = holder.itemView.context.getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        val curso = cursosList[position]
        Log.d("CursosAdapter", "Binding curso: ${curso.nombre} at position $position")
        holder.tituloTextView.text = curso.nombre
        holder.descripcionTextView.text = curso.descripcion
        // Cargar la imagen desde la URL del thumbnail
        Glide.with(holder.itemView.context)
            .load(curso.thumbnailURL) // O la URL de prueba
            .placeholder(R.drawable.thumbnail) // Imagen de placeholder
            .error(R.drawable.tacho_imagen) // Imagen si hay un error
            .into(holder.imagenPerfil) // Cargar en el ImageView

        // Cambiar color del rectángulo de estado según estado_curso
        when (curso.estado_curso) {
            1L -> holder.itemView.findViewById<View>(R.id.estado_rectangulo)
                .setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            2L -> holder.itemView.findViewById<View>(R.id.estado_rectangulo)
                .setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            3L -> holder.itemView.findViewById<View>(R.id.estado_rectangulo)
                .setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            else -> holder.itemView.findViewById<View>(R.id.estado_rectangulo)
                .setBackgroundColor(holder.itemView.context.getColor(android.R.color.transparent))
        }

        // Limpiamos los botones previamente añadidos para evitar duplicados
        holder.buttonContainer.removeAllViews()

        // Agregamos botones según el rol del usuario
        when (userRole) {
            "estudiante" -> {
                // Botón "Ver"
                val verButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_ver)
                    contentDescription = "Ver curso"
                    background = null // Eliminar fondo
                    //setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_purple))
                    setOnClickListener {
                        // Acción para ver el curso
                        val intent = Intent(context, vistaEspecificaCurso::class.java)
                        intent.putExtra("curso_Id", curso.id_curso)
                        holder.itemView.context.startActivity(intent)
                        Log.d("MisCursosAdapter", "Ver curso: ${curso.nombre}")
                    }
                }
                holder.buttonContainer.addView(verButton)

                // Botón "quitar de la lista"
                val quitarButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_quitar)
                    contentDescription = "quitar curso"
                    background = null // Eliminar fondo
                    setOnClickListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val isRemoved = withContext(Dispatchers.IO) {
                                        cursosDao.eliminarCursoDeUsuario(idUsuario.toLong(), curso.id_curso)
                                    }
                                    if (isRemoved) {
                                        onCursoRemoved()
                                        Toast.makeText(holder.itemView.context, "Curso eliminado de 'Mis Cursos'", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(holder.itemView.context, "No se pudo eliminar el curso", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: IllegalArgumentException) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(holder.itemView.context, e.message, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(holder.itemView.context, "Error al eliminar curso", Toast.LENGTH_SHORT).show()
                                    }
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                holder.buttonContainer.addView(quitarButton)
            }
            "gestor" -> {
                // Botón "Editar" en el rol "gestor"
                val editarButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_editar)
                    contentDescription = "Editar curso"
                    background = null // Eliminar fondo
                    setPadding(2, 0, 2, 0)
                    setOnClickListener {
                        Log.d("MisCursosAdapter", "Editar curso: ${curso.nombre}")

                        // Crear Intent para iniciar EditarCursosActivity y pasar los datos del curso
                        val intent = Intent(holder.itemView.context, EditarCursosActivity::class.java).apply {
                            putExtra("curso_id", curso.id_curso)
                            putExtra("curso_nombre", curso.nombre)
                            putExtra("curso_descripcion", curso.descripcion)
                            putExtra("curso_thumbnail", curso.thumbnailURL)
                            putExtra("curso_estado", curso.estado_curso)
                        }
                        holder.itemView.context.startActivity(intent)
                    }
                }
                holder.buttonContainer.addView(editarButton)
                val autoevaluacionButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_evaluacion)
                    contentDescription = "Agregar Evaluacion"
                    background = null // Eliminar fondo
                    setPadding(2, 0, 2, 0)
                    setOnClickListener {
                        val intent = Intent(holder.itemView.context, AgregarAutoevaluacionActivity::class.java)
                        intent.putExtra("cursoId", curso.id_curso)
                        holder.itemView.context.startActivity(intent)
                        Log.d("MisCursosAdapter", "Redirigiendo a agregar autoevaluación para el curso: ${curso.nombre}")
                        // Acción para editar el curso
                        Log.d("MisCursosAdapter", "Agregar Autoevaluacion curso: ${curso.nombre}")
                    }
                }
                holder.buttonContainer.addView(autoevaluacionButton)

                val editarEtapaButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_editar_etapa)
                    contentDescription = "Editar etapa"
                    background = null // Eliminar fondo
                    setPadding(2, 0, 2, 0)
                    setOnClickListener {
                        val intent = Intent(holder.itemView.context, AgregarEtapaActivity::class.java)
                        intent.putExtra("cursoId", curso.id_curso)
                        holder.itemView.context.startActivity(intent)
                        Log.d("MisCursosAdapter", "Editar etapa: ${curso.nombre}")
                    }
                }
                holder.buttonContainer.addView(editarEtapaButton)
            }
            // Agregar lógica para otros roles (admin, etc.)
            "admin" -> {
                val gestionarButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_editar)
                    contentDescription = "Gestionar Estado Curso"
                    background = null // Eliminar fondo
                    setPadding(0, 0, 0, 0)
                    setOnClickListener {
                        // Acción para gestionar el curso
                        Log.d("MisCursosAdapter", "Gestionar curso: ${curso.nombre}")
                    }
                }
                holder.buttonContainer.addView(gestionarButton)
            }
        }
    }

    override fun getItemCount(): Int {
        return cursosList.size
    }
}
