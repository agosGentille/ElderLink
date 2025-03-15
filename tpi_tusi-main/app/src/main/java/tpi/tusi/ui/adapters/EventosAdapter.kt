package tpi.tusi.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.EventosDao
import tpi.tusi.ui.activities.AgregarModificarEventoActivity
import tpi.tusi.ui.activities.VerEventoActivity
import tpi.tusi.ui.entities.Eventos

val eventosDao = EventosDao()

class EventosAdapter(
    private val listaEventos: List<Eventos>,
    private val userRole: Int,
    private val context: Context
) : RecyclerView.Adapter<EventosAdapter.EventosViewHolder>() {

    class EventosViewHolder(view: View, userRole: Int) : RecyclerView.ViewHolder(view) {
        val tituloTextView: TextView = view.findViewById(R.id.item_titulo)
        val descripcionTextView: TextView = view.findViewById(R.id.item_descripcion)
        val imagen: ImageView = view.findViewById(R.id.item_imagen)
        val btnVer: ImageView = view.findViewById(R.id.buttom_ver)
        val btnEliminar: ImageView? = if (userRole == 1) view.findViewById(R.id.boton_eliminar) else null
        val btnEditar: ImageView? = if (userRole == 1) view.findViewById(R.id.buttom_editar) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventosViewHolder {
        val layoutId = if (userRole == 1) R.layout.item_evento_admin else R.layout.item_evento_alumno
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return EventosViewHolder(view, userRole)
    }

    override fun onBindViewHolder(holder: EventosViewHolder, position: Int) {
        val eventos = listaEventos[position]
        holder.tituloTextView.text = eventos.titulo
        holder.descripcionTextView.text = eventos.descripcion

        holder.btnVer.setOnClickListener {
            val intent = Intent(context, VerEventoActivity::class.java)
            intent.putExtra("evento", eventos.id_evento)
            context.startActivity(intent)
        }

        Glide.with(holder.itemView.context)
            .load(eventos.url_imagen)
            .placeholder(R.drawable.thumbnail)
            .error(R.drawable.tacho_imagen)
            .into(holder.imagen)

        if(userRole == 1){
            holder.btnEliminar!!.setOnClickListener {
                AlertDialog.Builder(context)
                    .setMessage("¿Estás seguro de eliminar este evento?")
                    .setPositiveButton("Sí") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = withContext(Dispatchers.IO) {
                                val result = eventosDao.eliminarEvento(eventos.id_evento!!)
                                result
                            }

                            if (success) {
                                val currentPosition = holder.adapterPosition
                                if (currentPosition != RecyclerView.NO_POSITION) {
                                    (listaEventos as MutableList).removeAt(currentPosition)
                                    notifyItemRemoved(currentPosition)
                                }
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            holder.btnEditar!!.setOnClickListener{
                val intent = Intent(context, AgregarModificarEventoActivity::class.java)
                intent.putExtra("evento", eventos.id_evento)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return listaEventos.size
    }
}
