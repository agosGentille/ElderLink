package tpi.tusi.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.ui.activities.MainGestorActivity
import tpi.tusi.ui.entities.Cursos
import tpi.tusi.ui.entities.Etiquetas
import tpi.tusi.ui.utils.ImageUploadUtil
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CrearCursoFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private val cursoDaoDos = CursosDao()
    private val etiquetasDao = EtiquetasDao()
    private lateinit var imageView: ImageView
    private lateinit var nombreCursoEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var checkboxContainer: LinearLayout
    private var etiquetasList: List<Etiquetas> = listOf()
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null // Almacena la URI de la imagen seleccionada

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CrearCursoFragment", "onCreateView llamado")
        val view = inflater.inflate(R.layout.fragment_crear_curso, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        // Inicializar vistas
        imageView = view.findViewById(R.id.imageView2)
        nombreCursoEditText = view.findViewById(R.id.txtNombreCurso)
        descripcionEditText = view.findViewById(R.id.etxDescripcion)
        checkboxContainer = view.findViewById(R.id.checkboxContainer)
        // Cargar etiquetas en el Spinner

        // Cargar etiquetas de forma asíncrona
        lifecycleScope.launch {
            cargarEtiquetasEnCheckboxes()
        }

        imageView.setOnClickListener {
            openImageChooser()
        }

        val buttonAgregarCursos: Button = view.findViewById(R.id.btncrearCurso)
        buttonAgregarCursos.setOnClickListener {
            lifecycleScope.launch {
                if (validateForm()) { // Validar antes de proceder
                    showProgressDialog()
                    imageUri?.let { uri ->
                        val imagePath = ImageUploadUtil.uploadImageToApi(requireContext(), uri)
                        if (imagePath != null) {
                            AgregarCurso(imagePath)
                        } else {
                            showToast("No se pudo obtener la URL de la imagen.")
                        }
                    } ?: showToast("No se ha seleccionado una imagen")
                    hideProgressDialog()
                }
            }
        }
        return view
    }

    private suspend fun cargarEtiquetasEnCheckboxes() {
        // Obtener etiquetas de la base de datos
        val etiquetas = etiquetasDao.obtenerEtiquetas()

        // Crear un CheckBox por cada etiqueta y agregarlo al contenedor
        etiquetas.forEach { etiqueta ->
            val checkBox = CheckBox(requireContext()).apply {  // Usar 'requireContext()' para obtener el contexto
                text = etiqueta.nombre
                id = etiqueta.id_etiqueta.toInt()
            }
            checkboxContainer.addView(checkBox)
        }
    }
    private fun validateForm(): Boolean {
        val nombreCurso = nombreCursoEditText.text.toString().trim()
        val descripcion = descripcionEditText.text.toString().trim()

        // Validación para el nombre del curso
        if (nombreCurso.isEmpty()) {
            nombreCursoEditText.error = "El nombre del curso es obligatorio"
            return false
        } else if (nombreCurso.length > 25) {
            nombreCursoEditText.error = "El nombre del curso no debe exceder los 25 caracteres"
            return false
        }

        // Validación para la descripción
        if (descripcion.isEmpty()) {
            descripcionEditText.error = "La descripción es obligatoria"
            return false
        } else if (descripcion.length > 75) {
            descripcionEditText.error = "La descripción no debe exceder los 75 caracteres"
            return false
        }

        // Validación para las etiquetas (CheckBoxes)
        var isEtiquetaSeleccionada = false
        for (i in 0 until checkboxContainer.childCount) {
            val checkBox = checkboxContainer.getChildAt(i) as? CheckBox
            if (checkBox != null && checkBox.isChecked) {
                isEtiquetaSeleccionada = true
                break
            }
        }

        if (!isEtiquetaSeleccionada) {
            showToast("Debe seleccionar al menos una etiqueta")
            return false
        }

        return true
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private var progressDialog: ProgressDialog? = null

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Subiendo imagen, por favor espere...")
            setCancelable(false)
            show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/png"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                imageView.setImageURI(uri) // Muestra la imagen seleccionada
            }
        }
    }

    private fun AgregarCurso(thumbnailUrl: String) {
        // Obtener idUsuario desde SharedPreferences
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()
        val nombreCurso = nombreCursoEditText.text.toString().trim()
        val descripcion = descripcionEditText.text.toString().trim()

        lifecycleScope.launch {
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val fechaHoraActual = LocalDateTime.now().format(dateTimeFormatter)

            val nuevoCurso = Cursos(
                id_curso = 0,  // ID será generado automáticamente por la base de datos
                nombre = nombreCurso,
                descripcion = descripcion,
                fechaCreacion = Instant.now(),
                fechaActualizacion = Instant.now(),
                observacion = "Observación del curso de ejemplo.",
                usuario = idUsuario.toLong(),
                estado_curso = 3,
                thumbnailURL = thumbnailUrl
            )

            // Obtener las etiquetas seleccionadas
            val etiquetasSeleccionadas = obtenerEtiquetasSeleccionadas()

            if (etiquetasSeleccionadas.isNotEmpty()) {
                // Insertar el curso con las etiquetas seleccionadas
                val exitoCurso = cursoDaoDos.insertarCurso(nuevoCurso, etiquetasSeleccionadas)

                if (exitoCurso) {
                    Log.d("CrearCursoFragment", "Curso y etiquetas agregados exitosamente.")
                    // Redirigir a la pantalla principal
                    val intent = Intent(requireContext(), MainGestorActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    showToast("Error al agregar el curso y las etiquetas.")
                    Log.e("CrearCursoFragment", "Error al agregar el curso con etiquetas.")
                }
            } else {
                showToast("Debe seleccionar al menos una etiqueta.")
            }
        }
    }
    private suspend fun obtenerEtiquetasSeleccionadas(): List<Long> {
        val etiquetasSeleccionadas = mutableListOf<Long>()

        // Recorrer los CheckBox en checkboxContainer
        for (i in 0 until checkboxContainer.childCount) {
            val checkBox = checkboxContainer.getChildAt(i) as? CheckBox
            if (checkBox != null && checkBox.isChecked) {
                // Agregar el id_etiqueta al listado si está seleccionado
                etiquetasSeleccionadas.add(checkBox.id.toLong())
            }
        }

        return etiquetasSeleccionadas
    }

}
