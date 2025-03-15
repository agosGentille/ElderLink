package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import tpi.tusi.R
import tpi.tusi.data.dao.CursoDAO
import tpi.tusi.data.daos.AutoevaluacionesDao
import tpi.tusi.data.daos.PreguntasDao
import tpi.tusi.ui.entities.Preguntas

class AgregarPreguntaAutoevaluacionActivity : BaseActivity() {

    private lateinit var editTextPregunta: EditText
    private lateinit var editTextRespuestaCorrecta: EditText
    private lateinit var editTextRespuestaIncorrecta1: EditText
    private lateinit var editTextRespuestaIncorrecta2: EditText
    private lateinit var editTextRespuestaIncorrecta3: EditText
    private var cursoId: Long = 0L
    private var preguntaId: Long = 0L
    private var pregunta: String = ""
    private var preguntaPosicion: Int = 1
    private var preguntasList = mutableListOf<String>()
    private val pDao = PreguntasDao()
    private val aDao = AutoevaluacionesDao()
    private val cDao = CursoDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btnGuardarPregunta: Button = findViewById(R.id.button_guardar_pregunta)
        val btnCancelar: Button = findViewById(R.id.button_cancelar)

        cursoId = intent.getLongExtra("cursoId", -1L)
        preguntaId = intent.getLongExtra("preguntaId", -1L)
        pregunta = intent.getStringExtra("pregunta") ?: ""
        preguntaPosicion = intent.getIntExtra("preguntaPosicion", -1)
        preguntasList = intent.getStringArrayListExtra("preguntas_$cursoId") ?: arrayListOf<String>()

        editTextPregunta = findViewById(R.id.editText_pregunta)
        editTextRespuestaCorrecta = findViewById(R.id.editText_respuesta_correcta)
        editTextRespuestaIncorrecta1 = findViewById(R.id.editText_respuesta_incorrecta1)
        editTextRespuestaIncorrecta2 = findViewById(R.id.editText_respuesta_incorrecta2)
        editTextRespuestaIncorrecta3 = findViewById(R.id.editText_respuesta_incorrecta3)

        if(pregunta != "") cargarDatosEnFormulario(pregunta)

        btnGuardarPregunta.setOnClickListener {
            if(preguntaPosicion != -1){
                modificarPregunta(preguntaPosicion)
            }else{
                agregarPregunta()
            }
        }

        btnCancelar.setOnClickListener {
            val intent = Intent(this, AgregarAutoevaluacionActivity::class.java)
            intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
            intent.putExtra("cursoId", cursoId)
            startActivity(intent)
        }

    }

    private fun modificarPregunta(posicion: Int) {
        val pregunta = editTextPregunta.text.toString()
        val respuestaCorrecta = editTextRespuestaCorrecta.text.toString()
        val respuestaIncorrecta1 = editTextRespuestaIncorrecta1.text.toString()
        val respuestaIncorrecta2 = editTextRespuestaIncorrecta2.text.toString()
        val respuestaIncorrecta3 = editTextRespuestaIncorrecta3.text.toString()

        if (posicion in preguntasList.indices) {
            if (preguntaId != -1L) {
                lifecycleScope.launch {
                    val autoevaluacion = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
                    if (autoevaluacion != null) {
                        val nuevaPregunta = Preguntas(
                            id_pregunta = preguntaId,
                            pregunta = pregunta,
                            respuestaCorrecta = respuestaCorrecta,
                            respuestaIncorrecta1 = respuestaIncorrecta1,
                            respuestaIncorrecta2 = respuestaIncorrecta2,
                            respuestaIncorrecta3 = respuestaIncorrecta3,
                            estado = true,
                            fk_autoevaluaciones = autoevaluacion.id_autoevaluacion
                        )
                        val resultado = pDao.updatePregunta(nuevaPregunta)
                        if (resultado) {
                            Toast.makeText(this@AgregarPreguntaAutoevaluacionActivity, "Pregunta actualizada correctamente", Toast.LENGTH_SHORT).show()

                            preguntasList[posicion] = "$pregunta|$respuestaCorrecta|$respuestaIncorrecta1|$respuestaIncorrecta2|$respuestaIncorrecta3"
                            guardarPreguntasActualizadasEnSharedPreferences()

                            val intent = Intent(this@AgregarPreguntaAutoevaluacionActivity, AgregarAutoevaluacionActivity::class.java)
                            intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
                            intent.putExtra("cursoId", cursoId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@AgregarPreguntaAutoevaluacionActivity, "Error al actualizar la pregunta", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                val nuevaPregunta = "$pregunta|$respuestaCorrecta|$respuestaIncorrecta1|$respuestaIncorrecta2|$respuestaIncorrecta3"

                preguntasList[posicion] = nuevaPregunta
                guardarPreguntasActualizadasEnSharedPreferences()

                Toast.makeText(this@AgregarPreguntaAutoevaluacionActivity, "Pregunta modificada correctamente en SharedPreferences", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AgregarPreguntaAutoevaluacionActivity, AgregarAutoevaluacionActivity::class.java)
                intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
                intent.putExtra("cursoId", cursoId)
                startActivity(intent)
            }
        }
    }

    private fun guardarPreguntasActualizadasEnSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AutoevaluacionPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val preguntasJson = JSONArray()
        preguntasList.forEach { pregunta ->
            preguntasJson.put(pregunta)
        }

        editor.putString("preguntas_$cursoId", preguntasJson.toString())
        editor.apply()
    }
    private fun cargarDatosEnFormulario(pregunta: String) {
        val partes = pregunta.split("|")
        editTextPregunta.text = Editable.Factory.getInstance().newEditable(partes[0])
        editTextRespuestaCorrecta.text = Editable.Factory.getInstance().newEditable(partes[1])
        editTextRespuestaIncorrecta1.text = Editable.Factory.getInstance().newEditable(partes[2])
        editTextRespuestaIncorrecta2.text = Editable.Factory.getInstance().newEditable(partes[3])
        editTextRespuestaIncorrecta3.text = Editable.Factory.getInstance().newEditable(partes[4])
    }

    private fun agregarPregunta() {
        val pregunta = editTextPregunta.text.toString()
        val respuestaCorrecta = editTextRespuestaCorrecta.text.toString()
        val respuestaIncorrecta1 = editTextRespuestaIncorrecta1.text.toString()
        val respuestaIncorrecta2 = editTextRespuestaIncorrecta2.text.toString()
        val respuestaIncorrecta3 = editTextRespuestaIncorrecta3.text.toString()

        if (validarCampos()) {

            val nuevaPregunta = "$pregunta|$respuestaCorrecta|$respuestaIncorrecta1|$respuestaIncorrecta2|$respuestaIncorrecta3"

            guardarPreguntaEnSharedPreferences(nuevaPregunta)
            agregarPreguntaTemporal(nuevaPregunta)
            preguntasList.add(nuevaPregunta)
            val intent = Intent(this, AgregarAutoevaluacionActivity::class.java)
            intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
            intent.putExtra("cursoId", cursoId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Por favor, completa todos los campos necesarios", Toast.LENGTH_SHORT).show()
        }
    }

    fun validarCampos(): Boolean{
        val pregunta = editTextPregunta.text.toString()
        val respuestaCorrecta = editTextRespuestaCorrecta.text.toString()
        val respuestaIncorrecta1 = editTextRespuestaIncorrecta1.text.toString()
        val respuestaIncorrecta2 = editTextRespuestaIncorrecta2.text.toString()
        val respuestaIncorrecta3 = editTextRespuestaIncorrecta3.text.toString()

        var camposValidos = true

        if (pregunta.isEmpty()) {
            editTextPregunta.error = "Por favor, ingrese la pregunta"
            camposValidos = false
        } else {
            editTextPregunta.error = null
        }

        if (respuestaCorrecta.isEmpty()) {
            editTextRespuestaCorrecta.error = "Por favor, ingrese la respuesta correcta"
            camposValidos = false
        } else {
            editTextRespuestaCorrecta.error = null
        }

        if (respuestaIncorrecta1.isEmpty()) {
            editTextRespuestaIncorrecta1.error = "Por favor, ingrese una respuesta incorrecta"
            camposValidos = false
        } else {
            editTextRespuestaIncorrecta1.error = null
        }

        if (respuestaIncorrecta2.isEmpty()) {
            editTextRespuestaIncorrecta2.error = "Por favor, ingrese una respuesta incorrecta"
            camposValidos = false
        } else {
            editTextRespuestaIncorrecta2.error = null
        }

        if (respuestaIncorrecta3.isEmpty()) {
            editTextRespuestaIncorrecta3.error = "Por favor, ingrese una respuesta incorrecta"
            camposValidos = false
        } else {
            editTextRespuestaIncorrecta3.error = null
        }
        return camposValidos
    }

    private fun guardarPreguntaEnSharedPreferences(nuevaPregunta: String) {
        val sharedPreferences = getSharedPreferences("AutoevaluacionPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val preguntasGuardadasJson = sharedPreferences.getString("preguntas_$cursoId", "[]")
        val preguntasGuardadasArray = JSONArray(preguntasGuardadasJson)

        preguntasGuardadasArray.put(nuevaPregunta)

        editor.putString("preguntas_$cursoId", preguntasGuardadasArray.toString())
        editor.apply()
    }

    private fun agregarPreguntaTemporal(pregunta: String) {
        val sharedPreferences = getSharedPreferences("mis_preguntas_temp_$cursoId", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val preguntasTemp = obtenerPreguntasTemp().toMutableList()

        preguntasTemp.add(pregunta)
        editor.putString("preguntas_temp_$cursoId", preguntasTemp.joinToString("|"))
        editor.apply()
    }

    private fun obtenerPreguntasTemp(): List<String> {
        val sharedPreferences = getSharedPreferences("mis_preguntas_temp_$cursoId", MODE_PRIVATE)
        val preguntasString = sharedPreferences.getString("preguntas_temp_$cursoId", "") ?: ""

        return if (preguntasString.isEmpty()) {
            emptyList()
        } else {
            preguntasString.split("|")
        }
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_agregar_pregunta_autoevaluacion
    }

}