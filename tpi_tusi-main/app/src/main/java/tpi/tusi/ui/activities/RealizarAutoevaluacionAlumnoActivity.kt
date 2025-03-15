package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.AutoevaluacionesDao
import tpi.tusi.data.daos.PreguntasDao
import tpi.tusi.ui.adapters.PreguntasSinRevelarAdapter
import tpi.tusi.ui.adapters.eventosDao
import tpi.tusi.ui.entities.NotasUsuarios
import tpi.tusi.ui.entities.Preguntas
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.time.LocalDate

class RealizarAutoevaluacionAlumnoActivity : BaseActivity() {

    private lateinit var lvPreguntas: ListView
    private lateinit var btnFinalizar: Button
    private lateinit var tvAutoevaluacion: TextView

    private lateinit var sharedPreferences: SharedPreferences

    private var cursoId: Long = 0L
    private var autoevalId: Long = 0L
    private var rolesUsuario: MutableSet<String>? = null
    private var nombreCurso: String = ""
    private var autoevaluacionDao = AutoevaluacionesDao()
    private var preguntasDao = PreguntasDao()

    private val respuestasSeleccionadas = mutableMapOf<Long, String>() // Almacenará las respuestas seleccionadas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realizar_autoevaluacion_alumno)

        lvPreguntas = findViewById(R.id.lvPreguntas)
        btnFinalizar = findViewById(R.id.button_finalizar_autoevaluacion)
        tvAutoevaluacion = findViewById(R.id.textView_autoevaluacion)

        val sharedPreferences = this.getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("id", 0L)
        rolesUsuario = sharedPreferences.getStringSet("roles", null)

        cursoId = intent.getLongExtra("cursoId", 0L)
        if (cursoId == 0L) {
            Toast.makeText(this, "Error al recibir el ID del curso", Toast.LENGTH_SHORT).show()
            finish()
        }

        nombreCurso = intent.getStringExtra("nombreCurso")!!
        tvAutoevaluacion.text = "Autoevaluación: $nombreCurso"
        mostrarPreguntas()

        btnFinalizar.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("¿Estás seguro que quieres finalizar la autoevaluación?")
                .setPositiveButton("Sí") { _, _ ->
                    val nota = calcularNota()

                    val notasUsuarios = NotasUsuarios(
                        id_nota = 0L,
                        fecha = Date.valueOf(LocalDate.now().toString()),
                        fk_autoevaluacion = autoevalId,
                        calificacion = nota.toInt(),
                        fk_usuario = userId,
                        nro_intento = 0
                    )
                    lifecycleScope.launch {
                        if(autoevaluacionDao.cargarNotaAutoevaluacion(notasUsuarios)){
                            val intent = Intent(this@RealizarAutoevaluacionAlumnoActivity, NotaFinalAutoevaluacionActivity::class.java)
                            intent.putExtra("autoevalId", autoevalId)
                            intent.putExtra("usuarioId", userId)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(this@RealizarAutoevaluacionAlumnoActivity, "Error al cargar la nota", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                .setNegativeButton("No", null)
                .show()
        }
        bloquearControlesAdminGestor()
    }

    private fun bloquearControlesAdminGestor(){
        if (rolesUsuario!!.contains("estudiante")) {
            btnFinalizar.isEnabled = true
        } else{
            btnFinalizar.isEnabled = false
        }
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_realizar_autoevaluacion_alumno
    }

    private fun mostrarPreguntas() {
        lifecycleScope.launch {
            val autoevaluacion = autoevaluacionDao.obtenerAutoevaluacionPorCursoId(cursoId)
            autoevalId = autoevaluacion!!.id_autoevaluacion
            val listaPreguntas = preguntasDao.obtenerPreguntasPorAutoevaluacionId(autoevalId)

            val preguntasSinRevelarAdapter = PreguntasSinRevelarAdapter(this@RealizarAutoevaluacionAlumnoActivity, listaPreguntas, respuestasSeleccionadas)
            lvPreguntas.adapter = preguntasSinRevelarAdapter
        }
    }

    private fun calcularNota(): Double {
        val preguntas = (lvPreguntas.adapter as PreguntasSinRevelarAdapter).getPreguntas()

        val respuestasCorrectas = preguntas.count { pregunta ->
            val respuestaSeleccionada = respuestasSeleccionadas[pregunta.id_pregunta]
            respuestaSeleccionada == pregunta.respuestaCorrecta
        }

        val porcentaje = (respuestasCorrectas.toDouble() / preguntas.size) * 100

        return BigDecimal(porcentaje).setScale(2, RoundingMode.HALF_UP).toDouble()
    }



}
