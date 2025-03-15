package tpi.tusi.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.ui.entities.Cursos
import tpi.tusi.ui.utils.ImageUploadUtil
import java.time.Instant

class EditarCursoFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var txtNombreCurso: EditText
    private lateinit var etxDescripcion: EditText
    private lateinit var tituloTextView: TextView
    private lateinit var checkboxContainer: LinearLayout
    private lateinit var imageViewPortada: ImageView
    private lateinit var butonEditar: Button
    private lateinit var editButtonsContainer : LinearLayout
    private lateinit var btnCancelarCurso: Button

    private val cursoDao = CursosDao()
    private val etiquetasDao = EtiquetasDao()
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_curso, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        // Obtener cursoId desde los argumentos
        val cursoId = arguments?.getLong("curso_id") ?: -1
        Log.d("EditarCursoFragment", "cursoId recibido: $cursoId")

        // Obtener referencias de los campos de entrada
        txtNombreCurso = view.findViewById(R.id.txtNombreCurso)
        etxDescripcion = view.findViewById(R.id.etxDescripcion)
        tituloTextView = view.findViewById(R.id.textView6)
        imageViewPortada = view.findViewById(R.id.imageView2)
        setupImageView()

        checkboxContainer = view.findViewById(R.id.checkboxContainer)
        butonEditar = view.findViewById(R.id.btncrearCurso)

        // Ajustar el título en función del modo (Crear o Editar)
        val modoEdicion = arguments?.getBoolean("modo_edicion") ?: false
        Log.d("EditarCursoFragment", "Modo edición: $modoEdicion")

        tituloTextView.text = if (modoEdicion) "Modificar Curso" else "Crear Curso"
        butonEditar.text = if (modoEdicion) "Modificar Curso" else "Guardar Curso"

        // Cargar los datos si es modo edición
        if (modoEdicion) {
            loadCursoData()
        }

        // Cargar etiquetas de forma asíncrona
        lifecycleScope.launch {
            if (cursoId != -1L) {
                cargarEtiquetasEnCheckboxes(cursoId)
            }
        }

        // Cambiar imagen:
        imageViewPortada.setOnClickListener {
            openImageChooser()
        }

        // Configurar la acción del botón de edición
        butonEditar.setOnClickListener {
            actualizarCurso(cursoId)
        }
        // Inicializar los botones de edición
        //btnEditarEtapa = view.findViewById(R.id.btnEditar)
        btnCancelarCurso = view.findViewById(R.id.btnEliminar)
        editButtonsContainer = view.findViewById(R.id.edit_buttons_container)

        // Configura la visibilidad del contenedor de botones
        editButtonsContainer.visibility = if (modoEdicion) View.VISIBLE else View.GONE

        // Configura los listeners de los botones
        //aca se le daria inicio a los botones de editarAutoEvaluacion y Etapas
        btnCancelarCurso.setOnClickListener{
            mostrarDialogoConfirmacionCancelacion(cursoId)
        }
        return view
    }

    private fun setupImageView() {

        val heightInDp = 200
        val heightInPixels = (heightInDp * resources.displayMetrics.density).toInt()

        imageViewPortada.layoutParams = imageViewPortada.layoutParams.apply {
            height = heightInPixels
        }
    }

    private fun loadCursoData() {
        arguments?.let { args ->
            val cursoNombre = args.getString("curso_nombre")
            val cursoDescripcion = args.getString("curso_descripcion")
            val cursoThumbnail = args.getString("curso_thumbnail")

            txtNombreCurso.setText(cursoNombre)
            etxDescripcion.setText(cursoDescripcion)

            if (!cursoThumbnail.isNullOrEmpty()) {
                // Calcula las dimensiones en pixels
                val heightInDp = 200
                val heightInPixels = (heightInDp * resources.displayMetrics.density).toInt()
                val screenWidth = resources.displayMetrics.widthPixels

                Glide.with(this)
                    .load(cursoThumbnail)
                    .override(screenWidth, heightInPixels)
                    .centerCrop()
                    .placeholder(R.drawable.thumbnail)
                    .error(R.drawable.ic_quitar)
                    .into(imageViewPortada)
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/png"
        }
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), REQUEST_IMAGE_PICK)
    }

    // Cuando se selecciona una nueva imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                val heightInDp = 200
                val heightInPixels = (heightInDp * resources.displayMetrics.density).toInt()
                val screenWidth = resources.displayMetrics.widthPixels

                Glide.with(this)
                    .load(it)
                    .override(screenWidth, heightInPixels)
                    .centerCrop()
                    .into(imageViewPortada)
                Log.d("EditarCursoFragment", "Imagen seleccionada URI: $it")
            }
        }
    }

    private suspend fun cargarEtiquetasEnCheckboxes(idCurso: Long) {
        val todasLasEtiquetas = etiquetasDao.obtenerEtiquetas()
        val etiquetasDelCurso = etiquetasDao.obtenerEtiquetasPorCurso(idCurso)
        val etiquetasCursoIds = etiquetasDelCurso.map { it.id_etiqueta }.toSet()

        checkboxContainer.removeAllViews()

        todasLasEtiquetas.forEach { etiqueta ->
            val checkBox = CheckBox(requireContext()).apply {
                text = etiqueta.nombre
                id = etiqueta.id_etiqueta.toInt()
                isChecked = etiquetasCursoIds.contains(etiqueta.id_etiqueta)
            }
            checkboxContainer.addView(checkBox)
        }

        Log.d("EditarCursoFragment", "Etiquetas cargadas: ${etiquetasCursoIds.size}")
    }

    private fun actualizarCurso(cursoId: Long) {
        val nombreCurso = txtNombreCurso.text.toString()
        val descripcionCurso = etxDescripcion.text.toString()
        val etiquetasSeleccionadas = checkboxContainer.children
            .filterIsInstance<CheckBox>()
            .filter { it.isChecked }
            .map { it.id.toLong() }
            .toList()

        // Obtener idUsuario desde SharedPreferences
        val idUsuario = sharedPreferences.getLong("id", 0L).toInt()

        lifecycleScope.launch {
            showProgressDialog()
            val imagePath = if (imageUri != null) {
                ImageUploadUtil.uploadImageToApi(requireContext(), imageUri!!)
            } else {
                arguments?.getString("curso_thumbnail")
            }

            if (imagePath != null) {
                val curso = Cursos(
                    id_curso = cursoId,
                    nombre = nombreCurso,
                    descripcion = descripcionCurso,
                    fechaCreacion = Instant.now(),
                    fechaActualizacion = Instant.now(),
                    observacion = "",
                    usuario = idUsuario.toLong(),
                    estado_curso = 3,
                    thumbnailURL = imagePath
                )

                val exito = cursoDao.actualizarCurso(curso, etiquetasSeleccionadas)
                if (exito) {
                    Log.d("EditarCursoFragment", "Curso y etiquetas guardadas exitosamente.")

                    val activityName = requireActivity().javaClass.simpleName
                    Log.d("EditarCursoFragment", "Finalizando la actividad: $activityName")

                    requireActivity().finish()
                } else {
                    Log.d("EditarCursoFragment", "Error al modificar curso y etiquetas.")
                    showToast("Error al modificar el curso")
                }
            } else {
                showToast("No se pudo obtener la URL de la imagen.")
            }

            hideProgressDialog()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

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

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
    private fun mostrarDialogoConfirmacionCancelacion(cursoId: Long) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación de Baja")
        builder.setMessage("Está a punto de dar de baja este curso. ¿Está seguro?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            // Llamar a la función de cancelación
            lifecycleScope.launch {
                val exito = cursoDao.gestionarEstadoCurso(2, cursoId)
                if (exito) {
                    showToast("Curso cancelado exitosamente.")
                    // Cierra la actividad o vuelve a la pantalla anterior
                    requireActivity().finish()
                } else {
                    showToast("Error al cancelar el curso.")
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}
