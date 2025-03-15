package tpi.tusi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import tpi.tusi.R
import tpi.tusi.ui.entities.Preguntas

class PreguntasAdapter(
    context: Context,
    private var preguntasList: MutableList<String>,
    private val onDelete: (Int) -> Unit,
    private val onUpdate: (Int) -> Unit
) : ArrayAdapter<String>(context, 0, preguntasList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val pregunta = getItem(position) ?: return convertView ?: View(context)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pregunta_autoevaluacion_revelada, parent, false)

        val partes = pregunta.split("|")
        val textViewNumeroPregunta = view.findViewById<TextView>(R.id.textView_numero_pregunta)
        val textViewPregunta = view.findViewById<TextView>(R.id.textView_pregunta)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val btnEliminarPregunta = view.findViewById<ImageButton>(R.id.btnEliminarPregunta)
        val btnModificarPregunta = view.findViewById<ImageButton>(R.id.btnModificarPregunta)

        textViewNumeroPregunta.text = "Pregunta N° ${position + 1}"
        textViewPregunta.text = partes[0]
        (radioGroup.getChildAt(0) as RadioButton).text = partes[1]
        (radioGroup.getChildAt(1) as RadioButton).text = partes[2]
        (radioGroup.getChildAt(2) as RadioButton).text = partes[3]
        (radioGroup.getChildAt(3) as RadioButton).text = partes[4]

        btnEliminarPregunta.setOnClickListener {
            onDelete(position)
        }

        btnModificarPregunta.setOnClickListener {
            onUpdate(position)
        }
        return view
    }

}