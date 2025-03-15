package tpi.tusi.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import tpi.tusi.R
import tpi.tusi.data.dao.CursoDAO
import tpi.tusi.data.daos.AutoevaluacionesDao
import tpi.tusi.data.daos.PreguntasDao
import tpi.tusi.ui.adapters.PreguntasAdapter
import tpi.tusi.ui.entities.Autoevaluacion
import tpi.tusi.ui.entities.Preguntas

class AgregarAutoevaluacionActivity : BaseActivity() {

    private lateinit var lvPreguntas: ListView
    private lateinit var preguntasAdapter: PreguntasAdapter
    private var preguntasList = mutableListOf<String>()
    private var cursoId: Long = 0L
    private val pDao = PreguntasDao()
    private val aDao = AutoevaluacionesDao()
    private val cDao = CursoDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lvPreguntas = findViewById(R.id.lvPreguntas)
        val buttonAgregarPregunta: ImageButton = findViewById(R.id.button_agregar_pregunta)
        val buttonGuardarCambios: Button = findViewById(R.id.button_agregar_autoevaluacion)
        val buttonEliminarAutoeval: Button = findViewById(R.id.button_eliminar_autoevaluacion)

        cursoId = intent.getLongExtra("cursoId", 0L)
        if (cursoId == 0L) {
            Toast.makeText(this, "Error al recibir el ID del curso", Toast.LENGTH_SHORT).show()
            finish()
        }

        //actualiza, en el shared preferences, las preguntas cargadas en la bd y
        //las preguntas "temporales" desde el shared preferences
        cargarPreguntas()

        preguntasAdapter = PreguntasAdapter(
            this,
            preguntasList,
            onDelete = { position -> eliminarPregunta(position) },
            onUpdate = { position -> modificarPregunta(position) }
        )

        lvPreguntas.adapter = preguntasAdapter

        //redirecciona al formulario para agregar preguntas
        buttonAgregarPregunta.setOnClickListener {
            val intent = Intent(this, AgregarPreguntaAutoevaluacionActivity::class.java)
            intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
            intent.putExtra("cursoId", cursoId)
            startActivityForResult(intent, REQUEST_CODE_ADD_PREGUNTA)
        }

        // Botón para guardar los cambios en la bd (insert y delete de preguntas)
        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        //No se realiza ningun cambio y vuelve al listado de cursos
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelarAutoevaluacion()
            }
        })

        buttonEliminarAutoeval.setOnClickListener(){
            eliminarAutoevaluacion()
        }
    }

    //función que no realiza ninguno de los cambios hechos en la autoeval. Solo
    //redirecciona al listado de cursos (Gestor)
    private fun cancelarAutoevaluacion() {
        val sharedPreferences = getSharedPreferences("AutoevaluacionPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        Toast.makeText(this, "La autoevaluación ha sido cancelada", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainGestorActivity::class.java))
    }

    //función que permite eliminar la pregunta del shPrefs y si es una pregunta
    // que existe en la bd, también ahí, por lo tanto tambien se borra de la vista.
    private fun eliminarPregunta(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirmación de Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar esta pregunta?")
            .setPositiveButton("Sí") { dialog, _ ->
                if (position in preguntasList.indices) {
                    val preguntaEliminada = preguntasList[position]
                    val enunciadoPregunta = preguntaEliminada.split("|")[0]
                    lifecycleScope.launch {
                        val autoeval = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
                        if(autoeval != null){
                            val preguntaEnBd = pDao.obtenerPreguntaConEnunciado(enunciadoPregunta, autoeval.id_autoevaluacion)

                            if (preguntaEnBd != null) {
                                pDao.deletePregunta(preguntaEnBd)
                            }
                        }
                        preguntasList.removeAt(position)
                        val preguntasSinDuplicados = preguntasList.toSet().toMutableList()
                        preguntasList.clear()
                        preguntasList.addAll(preguntasSinDuplicados)

                        guardarPreguntasEnSharedPreferences()
                        preguntasAdapter.notifyDataSetChanged()
                        Toast.makeText(this@AgregarAutoevaluacionActivity, "Pregunta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Ocurrió un error al eliminar la pregunta", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun modificarPregunta(position: Int) {
        val preguntaString = preguntasList[position]
        // Si tiene un ID en la bd, se envia; si no, se omite para indicar
        // que es una pregunta temporal
        val intent = Intent(this, AgregarPreguntaAutoevaluacionActivity::class.java)
        GlobalScope.launch {
            val autoevaluacion = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
            if (autoevaluacion != null) {
                val preguntaBD = pDao.obtenerPreguntaConEnunciado(
                    preguntaString.split("|")[0],
                    autoevaluacion.id_autoevaluacion
                )
                if (preguntaBD != null) intent.putExtra("preguntaId", preguntaBD.id_pregunta)
            }
            intent.putExtra("pregunta", preguntaString)
            intent.putStringArrayListExtra("preguntas_$cursoId", ArrayList(preguntasList))
            intent.putExtra("preguntaPosicion", position)
            intent.putExtra("cursoId", cursoId)

            startActivity(intent)
        }
    }

    //función que realiza todos los cambios en la bd (insert de autoeval y preguntas)
    private fun guardarCambios() {
        val nuevasPreguntas = mutableListOf<Preguntas>()
        lifecycleScope.launch {
            val autoevaluacionExistente = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
            if (autoevaluacionExistente != null) {
                //la autoevaluacion existe, por lo que no se genera una nueva en la bd.
                //solo se agregan las preguntas que NO existen en la bd (pero si en el ShPrefs)
                //y se actualiza el estado de la autoeval en TRUE (por si anteriormente se habia
                //eliminado)
                aDao.darDeAltaAutoevaluacion(autoevaluacionExistente)
                val preguntasExistentes = pDao.obtenerPreguntasPorAutoevaluacionId(autoevaluacionExistente.id_autoevaluacion)
                val idsPreguntasExistentes = preguntasExistentes.map { it.pregunta }

                preguntasList.forEach { pregunta ->
                    val campos = pregunta.split("|")
                    if (campos.size > 1) {
                        val enunciado = campos[0]

                        if (!idsPreguntasExistentes.contains(enunciado)) {
                            val nuevaPregunta = Preguntas(
                                id_pregunta = 0,
                                pregunta = enunciado,
                                respuestaCorrecta = campos[1],
                                respuestaIncorrecta1 = campos[2],
                                respuestaIncorrecta2 = campos[3],
                                respuestaIncorrecta3 = campos[4],
                                fk_autoevaluaciones = autoevaluacionExistente.id_autoevaluacion,
                                estado = true
                            )
                            nuevasPreguntas.add(nuevaPregunta)
                        }
                    }
                }
                if (nuevasPreguntas.isNotEmpty()) {
                    pDao.guardarPreguntas(nuevasPreguntas, autoevaluacionExistente.id_autoevaluacion)
                    Toast.makeText(this@AgregarAutoevaluacionActivity, "Autoevaluación modificada exitosamente", Toast.LENGTH_SHORT).show()
                    cDao.gestionarEstadoCurso(3, cursoId)

                } else {
                    Toast.makeText(this@AgregarAutoevaluacionActivity, "Debe agregar por lo menos una pregunta nueva para guardar cambios", Toast.LENGTH_SHORT).show()
                }
            } else {
                //no existe una autoeval para el curso seleccionado. Se procede a agregar
                //la autoeval y luego todas las preguntas
                val nuevaEvaluacion = Autoevaluacion(id_autoevaluacion = 0, fk_curso = cursoId, estado = true)
                val idNuevaEvaluacion = aDao.crearAutoevaluacion(nuevaEvaluacion)

                if (idNuevaEvaluacion != null) {
                    preguntasList.forEach { pregunta ->
                        val campos = pregunta.split("|")
                        if (campos.size > 1) {
                            val enunciado = campos[0]

                            val nuevaPregunta = Preguntas(
                                id_pregunta = 0,
                                pregunta = enunciado,
                                respuestaCorrecta = campos[1],
                                respuestaIncorrecta1 = campos[2],
                                respuestaIncorrecta2 = campos[3],
                                respuestaIncorrecta3 = campos[4],
                                fk_autoevaluaciones = idNuevaEvaluacion,
                                estado = true
                            )
                            nuevasPreguntas.add(nuevaPregunta)
                        }
                    }
                    if (nuevasPreguntas.isNotEmpty()) {
                        pDao.guardarPreguntas(nuevasPreguntas, idNuevaEvaluacion)
                    } else {
                        Toast.makeText(this@AgregarAutoevaluacionActivity, "Debe agregar por lo menos una pregunta nueva para guardar cambios", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(this@AgregarAutoevaluacionActivity, "Autoevaluación creada exitosamente", Toast.LENGTH_SHORT).show()
                    cDao.gestionarEstadoCurso(3, cursoId)
                } else {
                    Toast.makeText(this@AgregarAutoevaluacionActivity, "Ocurrió un error al crear la Autoevaluación", Toast.LENGTH_SHORT).show()
                }
            }
        }
        limpiarPreguntasTemp()
    }

    //limpia las preguntas temporales. Si existen preguntas que se obtienen desde la bd
    //y preguntas agregadas solo en la memoria interna, se eliminan las segundas.
    private fun limpiarPreguntasTemp() {
        val sharedPreferences = getSharedPreferences("mis_preguntas_temp_$cursoId", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_PREGUNTA && resultCode == RESULT_OK) {
            val preguntas = data?.getStringArrayListExtra("preguntas_$cursoId")
            preguntas?.let {
                preguntasList.clear()
                preguntasList.addAll(it)
                preguntasAdapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ADD_PREGUNTA = 1
    }

    //método abstracto definido en cada activity
    override fun obtenerPantalla(): Int {
        return R.layout.activity_agregar_autoevaluacion
    }

    //sirve para cargar las preguntas en el listview, las que vienen desde la bd y las del ShPrefs.
    private fun cargarPreguntas() {
        preguntasList.clear()
        lifecycleScope.launch {
            val autoevaluacionExistente = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
            val preguntasMostrar = mutableSetOf<String>()

            if (autoevaluacionExistente != null) {
                val preguntasExistentes = pDao.obtenerPreguntasPorAutoevaluacionId(autoevaluacionExistente.id_autoevaluacion)
                preguntasExistentes.forEach { pregunta ->
                    val preguntaString = "${pregunta.pregunta}|${pregunta.respuestaCorrecta}|${pregunta.respuestaIncorrecta1}|${pregunta.respuestaIncorrecta2}|${pregunta.respuestaIncorrecta3}"
                    preguntasMostrar.add(preguntaString)
                }
            }
            val sharedPreferences = getSharedPreferences("AutoevaluacionPrefs", MODE_PRIVATE)
            val preguntasJson = sharedPreferences.getString("preguntas_$cursoId", "[]") ?: "[]"
            val preguntasArray = JSONArray(preguntasJson)

            for (i in 0 until preguntasArray.length()) {
                preguntasMostrar.add(preguntasArray.getString(i))
            }

            preguntasList.addAll(preguntasMostrar)
            preguntasAdapter.notifyDataSetChanged()
        }
    }

    //almacena las nuevas preguntas en el ShPrefs.
    private fun guardarPreguntasEnSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AutoevaluacionPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val preguntasArray = JSONArray()
        preguntasList.forEach { pregunta ->
            preguntasArray.put(pregunta)
        }

        editor.putString("preguntas_$cursoId", preguntasArray.toString())
        editor.apply()
    }

    private fun eliminarAutoevaluacion() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación para Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar esta autoevaluación?")
            .setPositiveButton("Sí") { dialog, _ ->
                lifecycleScope.launch {
                    val autoevaluacion = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
                    if(autoevaluacion != null) {
                        val resultadoP = pDao.deletePreguntasPorAutoevaluacion(autoevaluacion)
                        val resultadoA = aDao.deleteAutoevaluacion(autoevaluacion)
                        if (resultadoP && resultadoA) {
                            Toast.makeText(this@AgregarAutoevaluacionActivity, "Autoevaluacion eliminada correctamente", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AgregarAutoevaluacionActivity, MainGestorActivity::class.java))
                        } else {
                            Toast.makeText(this@AgregarAutoevaluacionActivity, "Error al eliminar la autoevaluacion", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}