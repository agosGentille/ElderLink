package tpi.tusi.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.data.daos.EventosDao
import tpi.tusi.ui.entities.Eventos
import tpi.tusi.ui.utils.ImageUploadUtil
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarModificarEventoActivity : AppCompatActivity() {

    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var tableLayoutEtiquetas: TableLayout
    private lateinit var imageView: ImageView
    private lateinit var btnFecha: Button
    private lateinit var tvFecha: TextView
    private lateinit var btnCrear: Button
    private lateinit var btnModificar: Button
    private lateinit var buttonSelectImage: Button

    private var eventoModificar: Long = 0L

    private val PICK_IMAGE_REQUEST = 1
    private var urlImagen: Uri? = null
    private var pathImagen: String? = null

    private val etiquetasDao = EtiquetasDao()
    private val eventosDao = EventosDao()

    private val selectedEtiquetasIds = mutableListOf<Long>()

    private val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val formato2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_modificar_evento)

        eventoModificar = intent.getLongExtra("evento", 0L) //0 = crear; Otro valor = modificar (trae un id)

        etTitulo = findViewById(R.id.editText_titulo)
        etDescripcion = findViewById(R.id.editText_descripcion)
        tableLayoutEtiquetas = findViewById(R.id.tableLayout_etiquetas)
        imageView = findViewById(R.id.imageView_destacada)
        btnFecha = findViewById(R.id.buttom_fecha)
        tvFecha = findViewById(R.id.textView_fechaNac)
        btnCrear = findViewById(R.id.button_agregar)
        btnModificar = findViewById(R.id.button_modificar)

        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        buttonSelectImage.setOnClickListener {
            openImageChooser()
        }

        btnFecha.setOnClickListener {
            //Obtiene la fecha actual como predeterminada para el selector
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Muestra el DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Formato de fecha (puedes ajustarlo según tu preferencia)
                val fecha = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                tvFecha.text = fecha
            }, year, month, day)

            datePickerDialog.show()
        }

        if(eventoModificar == 0L) {
            btnCrear.setOnClickListener {
                crearEvento()
            }
        }
        else{
            setearDatos(eventoModificar)
            btnModificar.setOnClickListener {
                modificarEvento()
            }
            btnModificar.isEnabled = true
            btnModificar.visibility = View.VISIBLE
            btnCrear.visibility = View.INVISIBLE
            btnCrear.isEnabled = false
        }

        cargarEtiquetas()

    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/png"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            urlImagen = data.data
            imageView.setImageURI(urlImagen)
        }
    }

    private fun cargarEtiquetas() {
        selectedEtiquetasIds.clear()
        lifecycleScope.launch {
            val etiquetas = etiquetasDao.obtenerEtiquetas()
            var tableRow: TableRow? = null
            var checkBoxCount = 0

            for (etiqueta in etiquetas) {
                // Se crea una fila que contenga máximo 2 checkboxes
                if (checkBoxCount % 2 == 0) {
                    tableRow = TableRow(this@AgregarModificarEventoActivity) // Cambia a this@TuActividad
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                // Se crea el checkbox
                val checkBox = CheckBox(this@AgregarModificarEventoActivity).apply { // Cambia requireContext() a this@TuActividad
                    text = etiqueta.nombre
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    // Asociar el ID de la etiqueta con el CheckBox
                    tag = etiqueta.id_etiqueta // Guarda el ID en la propiedad tag
                }

                // Se agrega el checkbox a la row
                tableRow?.addView(checkBox)

                // Se aumenta el contador de checkboxes en uno
                checkBoxCount++

                // Si el número de checkboxes es par, se crea una nueva row
                if (checkBoxCount % 2 == 0) {
                    tableLayoutEtiquetas.addView(tableRow)
                }
            }

            if (checkBoxCount % 2 != 0 && tableRow != null) {
                tableLayoutEtiquetas.addView(tableRow)
            }

        }
    }

    private fun crearEvento(){

        if(!validarCamposNoVacios())
            return

        if(urlImagen==null){
            Toast.makeText(applicationContext, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val imagePath = ImageUploadUtil.uploadImageToApi(applicationContext, urlImagen!!)

            if(imagePath==null){
                Toast.makeText(applicationContext, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                return@launch
            }

            //Recoge los checkbox marcados de etiquetas
            for (i in 0 until tableLayoutEtiquetas.childCount) {
                val row = tableLayoutEtiquetas.getChildAt(i) as TableRow
                for (j in 0 until row.childCount) {
                    val checkBox = row.getChildAt(j) as CheckBox
                    if (checkBox.isChecked) {
                        val idEtiqueta = checkBox.tag as Long // Obtener el ID guardado en tag
                        selectedEtiquetasIds.add(idEtiqueta) // Agregar ID a la lista
                    }
                }
            }

            if(selectedEtiquetasIds.isEmpty()){
                Toast.makeText(applicationContext, "No se ha seleccionado ninguna etiqueta", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val evento = Eventos(
                id_evento = 0L,
                activo = true,
                titulo = etTitulo.text.toString(),
                fecha = Date(formato.parse(tvFecha.text.toString())!!.time),
                descripcion = etDescripcion.text.toString(),
                url_imagen = imagePath
            )

            if(eventosDao.crearEventos(evento, selectedEtiquetasIds)){
                Toast.makeText(applicationContext, "Se ha creado el evento exitosamente", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(applicationContext, "Ha ocurrido un error al crear el evento", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(applicationContext, ListadoEventosActivity::class.java)
            startActivity(intent)

        }

    }

    private fun setearDatos(idEvento:Long){
        lifecycleScope.launch {
            val evento = eventosDao.traerEvento(idEvento)
            if(evento!=null) {
                val fechaOriginal = formato2.parse(evento.fecha.toString())

                etTitulo.setText(evento.titulo)
                etDescripcion.setText(evento.descripcion)
                tvFecha.text = formato.format(fechaOriginal!!)
                //Setea imagen
                Glide.with(this@AgregarModificarEventoActivity)
                    .load(evento.url_imagen)
                    .placeholder(R.drawable.thumbnail)
                    .error(R.drawable.tacho_imagen)
                    .into(imageView)
                pathImagen = evento.url_imagen
                //Setea etiquetas
                setearEtiquetas(eventosDao.traerEtiquetasEvento(evento.id_evento!!))
            }
        }
    }

    private fun setearEtiquetas(chequeadas:List<Long>){
        for (i in 0 until tableLayoutEtiquetas.childCount) {
            val row = tableLayoutEtiquetas.getChildAt(i) as TableRow
            // Recorre los checkboxes en cada fila
            for (j in 0 until row.childCount) {
                val checkBox = row.getChildAt(j) as CheckBox
                val idEtiqueta = checkBox.tag as Long

                checkBox.isChecked = chequeadas.contains(idEtiqueta)
            }
        }
    }

    private fun modificarEvento(){

        if(!validarCamposNoVacios())
            return

        selectedEtiquetasIds.clear()

        //Recoge los checkbox marcados de etiquetas
        for (i in 0 until tableLayoutEtiquetas.childCount) {
            val row = tableLayoutEtiquetas.getChildAt(i) as TableRow
            for (j in 0 until row.childCount) {
                val checkBox = row.getChildAt(j) as CheckBox
                if (checkBox.isChecked) {
                    val idEtiqueta = checkBox.tag as Long // Obtener el ID guardado en tag
                    selectedEtiquetasIds.add(idEtiqueta) // Agregar ID a la lista
                }
            }
        }

        if(selectedEtiquetasIds.isEmpty()){
            Toast.makeText(applicationContext, "No se ha seleccionado ninguna etiqueta", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {

            val imagePath: String?

            if(urlImagen == null){
                imagePath = pathImagen
            }
            else {
                imagePath = ImageUploadUtil.uploadImageToApi(applicationContext, urlImagen!!)
                if(imagePath==null){
                    Toast.makeText(applicationContext, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val evento = Eventos(
                id_evento = 0L,
                activo = true,
                titulo = etTitulo.text.toString(),
                fecha = Date(formato.parse(tvFecha.text.toString())!!.time),
                descripcion = etDescripcion.text.toString(),
                url_imagen = imagePath
            )

            if(eventosDao.modificarEvento(eventoModificar, evento, selectedEtiquetasIds)){
                Toast.makeText(applicationContext, "Se ha modificado el evento exitosamente", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(applicationContext, "Ha ocurrido un error al modificar el evento", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(applicationContext, ListadoEventosActivity::class.java)
            startActivity(intent)

        }

    }

    private fun validarCamposNoVacios(): Boolean {
        val campos = listOf(
            etTitulo to "Título",
            etDescripcion to "Descripción",
            tvFecha to "Fecha"
        )

        for ((campo, nombreCampo) in campos) {
            if (campo.text.toString().isEmpty()) {
                Toast.makeText(this, "El campo $nombreCampo es obligatorio.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

}