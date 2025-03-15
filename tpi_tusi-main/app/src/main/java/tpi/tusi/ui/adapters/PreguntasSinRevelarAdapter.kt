package tpi.tusi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import tpi.tusi.R
import tpi.tusi.ui.entities.Preguntas

class PreguntasSinRevelarAdapter(
    private val context: Context,
    private val preguntas: List<Preguntas>,
    private val respuestasSeleccionadas: MutableMap<Long, String> // Este map almacenará las respuestas seleccionadas
) : ArrayAdapter<Preguntas>(context, 0, preguntas) {

    fun getPreguntas(): List<Preguntas> = preguntas // Método para obtener las preguntas

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_pregunta_autoevaluacion, parent, false)

        val pregunta = getItem(position)!!

        // Configura los elementos de la vista
        val numeroPregunta = view.findViewById<TextView>(R.id.textView_numero_pregunta)
        val enunciadoPregunta = view.findViewById<TextView>(R.id.textView_pregunta)
        val radioGroup = view.findViewById<RadioGroup>(R.id.rgRespuestas)

        numeroPregunta.text = "Pregunta N° ${position + 1}"
        enunciadoPregunta.text = pregunta.pregunta

        // Configura las opciones en los RadioButtons
        val opciones = listOf(
            pregunta.respuestaCorrecta,
            pregunta.respuestaIncorrecta1,
            pregunta.respuestaIncorrecta2,
            pregunta.respuestaIncorrecta3
        ).shuffled() // Mezcla las opciones para que la respuesta correcta no esté siempre en el mismo lugar

        // Asigna el texto a cada RadioButton y limpia el RadioGroup para evitar interferencias
        radioGroup.clearCheck()
        val radioButtons = listOf(
            view.findViewById<RadioButton>(R.id.radioButton_opcion1),
            view.findViewById<RadioButton>(R.id.radioButton_opcion2),
            view.findViewById<RadioButton>(R.id.radioButton_opcion3),
            view.findViewById<RadioButton>(R.id.radioButton_opcion4)
        )

        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.text = opciones[index]
        }

        // Configura el listener para capturar la selección
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val seleccionada = when (checkedId) {
                R.id.radioButton_opcion1 -> radioButtons[0].text.toString()
                R.id.radioButton_opcion2 -> radioButtons[1].text.toString()
                R.id.radioButton_opcion3 -> radioButtons[2].text.toString()
                R.id.radioButton_opcion4 -> radioButtons[3].text.toString()
                else -> null
            }

            // Almacena la respuesta seleccionada en el Map
            if (seleccionada != null) {
                respuestasSeleccionadas[pregunta.id_pregunta] = seleccionada
            }
        }

        return view
    }
}
