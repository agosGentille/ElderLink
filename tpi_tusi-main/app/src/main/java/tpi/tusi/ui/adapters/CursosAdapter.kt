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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.activities.GestionEstadoCursoActivity
import tpi.tusi.ui.activities.vistaEspecificaCurso
import tpi.tusi.ui.entities.Cursos

class CursosAdapter(
    private val cursosList: List<Cursos>,
    private val userRole: String,
) : RecyclerView.Adapter<CursosAdapter.CursoViewHolder>() {


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
                        Log.d("CursosAdapter", "Ver curso: $curso")
                    }
                }
                holder.buttonContainer.addView(verButton)

                // Botón "Agregar"
                val agregarButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_agregar)
                    contentDescription = "Agregar curso"
                    background = null
                    setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val isAdded = withContext(Dispatchers.IO) {
                                    cursosDao.agregarCursoAUsuario(idUsuario.toLong(), curso.id_curso)
                                }
                                if (isAdded) {
                                    Toast.makeText(holder.itemView.context, "Curso agregado a 'Mis Cursos'", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(holder.itemView.context, "Este Curso ya esta en tu lista", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: IllegalArgumentException) {
                                withContext(Dispatchers.Main) { // Aseguramos que el Toast se muestre en el hilo principal
                                    Toast.makeText(holder.itemView.context, e.message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(holder.itemView.context, "Error al agregar curso", Toast.LENGTH_SHORT).show()
                                }
                                e.printStackTrace()
                            }
                        }
                    }
                }
                holder.buttonContainer.addView(agregarButton)


            }
            "gestor" -> {
                // Puedes agregar otros botones o funciones según el rol del gestor
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
                        Log.d("CursosAdapter", "Ver curso: $curso")
                    }
                }
                holder.buttonContainer.addView(verButton)

            }
            // Agregar lógica para otros roles (admin, etc.)
            "admin" -> {
                val gestionarButton = ImageButton(holder.itemView.context).apply {
                    setImageResource(R.drawable.ic_editar)
                    contentDescription = "Gestionar Estado Curso"
                    background = null // Eliminar fondo
                    setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, GestionEstadoCursoActivity::class.java).apply {
                            putExtra("id", curso.id_curso)
                        }
                        context.startActivity(intent)
                    }
                }
                holder.buttonContainer.addView(gestionarButton)
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
                        Log.d("CursosAdapter", "Ver curso: $curso")
                    }
                }
                holder.buttonContainer.addView(verButton)
            }
        }
    }

    override fun getItemCount(): Int {
        return cursosList.size
    }
}
