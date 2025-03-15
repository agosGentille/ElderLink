package tpi.tusi.ui.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import tpi.tusi.ui.entities.Etapas
import tpi.tusi.R
import tpi.tusi.data.daos.EtapasDao
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class abmEtapasAdapter(
    private val context: Context,
    private val etapasList: MutableList<Etapas>
) : BaseAdapter() {

    override fun getCount(): Int = etapasList.size

    override fun getItem(position: Int): Any = etapasList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_agregar_etapa, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val etapa = etapasList[position]

        viewHolder.txtTitulo.removeTextChangedListener(viewHolder.txtTituloTextWatcher)
        viewHolder.edtDescripcion.removeTextChangedListener(viewHolder.edtDescripcionTextWatcher)


        viewHolder.txtTituloTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != etapa.titulo) {
                    etapa.titulo = newText // Actualiza solo si el texto cambia
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        viewHolder.txtTitulo.addTextChangedListener(viewHolder.txtTituloTextWatcher)



        viewHolder.edtDescripcionTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != etapa.contenido) {
                    etapa.contenido = newText // Actualiza solo si el texto cambia
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        viewHolder.edtDescripcion.addTextChangedListener(viewHolder.edtDescripcionTextWatcher)


        viewHolder.txtTitulo.setText(etapa.titulo)
        viewHolder.edtDescripcion.setText(etapa.contenido)


        viewHolder.btnModificarEtapa.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                etapa.titulo = viewHolder.txtTitulo.text.toString()
                etapa.contenido = viewHolder.edtDescripcion.text.toString()
                val etapaExistente = EtapasDao().getEtapaById(etapa.id_etapa)
                if (etapaExistente != null) {
                    if (validarCamposCompletos(view)) {
                        val exito = EtapasDao().updateEtapa(etapa)
                        if (exito) {
                            etapasList[position] = etapa
                            notifyDataSetChanged()
                            Toast.makeText(context, "Etapa modificada correctamente.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al modificar la etapa.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Complete todos los campos antes de modificar.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Para modificar, primero debes agregar la etapa.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewHolder.btnEliminarEtapa.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirmación de Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta pregunta?")
                .setPositiveButton("Sí") { dialog, _ ->
                    if (etapa.id_etapa != 0L) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val exito = EtapasDao().deleteEtapa(etapa.id_etapa)
                            if (exito) {
                                etapasList.removeAt(position)
                                notifyDataSetChanged()
                                Toast.makeText(context, "Pregunta eliminada correctamente.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al eliminar la pregunta.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        etapasList.removeAt(position)
                        notifyDataSetChanged()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }

        viewHolder.btnAgregarEtapa.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val existe = EtapasDao().getEtapaById(etapa.id_etapa)
                if (existe != null) {
                    Toast.makeText(context, "Esta pregunta ya existe, debe modificarse.", Toast.LENGTH_SHORT).show()
                } else {
                    if (validarCamposCompletos(view)) {
                        etapa.titulo = viewHolder.txtTitulo.text.toString()
                        etapa.contenido = viewHolder.edtDescripcion.text.toString()

                        val exito = EtapasDao().insertEtapa(etapa)
                        if (exito) {
                            etapasList.add(etapa)
                            notifyDataSetChanged()
                            Toast.makeText(context, "Pregunta agregada correctamente.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al agregar la pregunta.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Antes de agregar, complete todos los campos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }

    class ViewHolder(view: View) {
        val txtTitulo: EditText = view.findViewById(R.id.txtTitulo)
        val edtDescripcion: EditText = view.findViewById(R.id.edtDescripcion)
        val btnModificarEtapa: ImageButton = view.findViewById(R.id.btnModificarEtapa)
        val btnEliminarEtapa: ImageButton = view.findViewById(R.id.btnEliminarEtapa)
        val btnAgregarEtapa: ImageButton = view.findViewById(R.id.btnAgregarEtapa)

        var txtTituloTextWatcher: TextWatcher? = null
        var edtDescripcionTextWatcher: TextWatcher? = null
    }

    fun agregarNuevaInstanciaVacia(cursoId: Long) {
        etapasList.add(
            Etapas(
                id_etapa = 0L,
                titulo = "",
                contenido = "",
                activo = true,
                fk_curso = cursoId
            )
        )
        notifyDataSetChanged()
    }


    fun validarCamposCompletos(view: View): Boolean {
        val txtTitulo: EditText = view.findViewById(R.id.txtTitulo)
        val edtDescripcion: EditText = view.findViewById(R.id.edtDescripcion)

        var camposValidos = true

        if (txtTitulo.text.isNullOrEmpty()) {
            txtTitulo.error = "Por favor, ingrese el título"
            camposValidos = false
        }

        if (edtDescripcion.text.isNullOrEmpty()) {
            edtDescripcion.error = "Por favor, ingrese el contenido"
            camposValidos = false
        }

        return camposValidos
    }


    fun actualizarEtapas(nuevasEtapas: List<Etapas>) {
        etapasList.clear()
        etapasList.addAll(nuevasEtapas)
        Log.d("adapterEtapas", "Etapas List: $etapasList")
        notifyDataSetChanged()
    }

}