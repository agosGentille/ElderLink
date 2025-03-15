package tpi.tusi.ui.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.ReportesDao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReporteActivity : BaseActivity() {

    private lateinit var btnFechaInicio: Button
    private lateinit var btnFechaFinal: Button
    private lateinit var tvFechaInicio: TextView
    private lateinit var tvFechaFinal: TextView
    private lateinit var rgTipo: RadioGroup
    private lateinit var btnGenerar: Button

    private lateinit var dialogTitulo: TextView
    private lateinit var dialogDescripcion: TextView
    private lateinit var dialogFinalizar: Button

    private lateinit var dialog: Dialog
    private var rgReporte: RadioGroup? = null
    private var rgSeleccionado: RadioButton? = null
    private var reportesDao = ReportesDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnFechaInicio = findViewById(R.id.button_fechaInicio)
        btnFechaFinal = findViewById(R.id.button_fechaFinal)
        tvFechaInicio = findViewById(R.id.textView_fechaInicio)
        tvFechaFinal = findViewById(R.id.textView_fechaFinal)
        rgReporte = findViewById(R.id.rgTipo)
        btnGenerar = findViewById(R.id.btnGenerar)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_reportes)

        dialogTitulo = dialog.findViewById(R.id.tvTitulo)
        dialogDescripcion = dialog.findViewById(R.id.tvDescripcion)
        dialogFinalizar = dialog.findViewById(R.id.btnAceptar)

        dialogFinalizar.setOnClickListener {
            dialog.dismiss()
        }

        btnFechaInicio.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Muestra el DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Formato de fecha en yyyy-MM-dd
                val fecha = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                tvFechaInicio.text = fecha
            }, year, month, day)

            datePickerDialog.show()
        }

        btnFechaFinal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Muestra el DatePickerDialog
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Formato de fecha en yyyy-MM-dd
                val fecha = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                tvFechaFinal.text = fecha
            }, year, month, day)

            datePickerDialog.show()
        }

        btnGenerar.setOnClickListener {
            generarReporte()
        }

    }

    private fun validarCamposNoVacios(): Boolean {
        val campos = listOf(
            tvFechaInicio to "Fecha inicio",
            tvFechaFinal to "Fecha final"
        )

        for ((campo, nombreCampo) in campos) {
            if (campo.text.toString().isEmpty()) {
                Toast.makeText(this, "El campo $nombreCampo es obligatorio.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    fun validarFechas(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return try {
            val fechaInicio = dateFormat.parse(tvFechaInicio.text.toString())
            val fechaFinal = dateFormat.parse(tvFechaFinal.text.toString())

            if (fechaInicio != null && fechaFinal != null) {
                fechaInicio <= fechaFinal
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun generarReporte(){

        if(!validarCamposNoVacios())
            return

        if(!validarFechas()){
            Toast.makeText(this, "La fecha final debe ser posterior o igual a la inicial", Toast.LENGTH_LONG).show()
            return
        }

        if (rgReporte?.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Por favor, seleccione un tipo de reporte antes de continuar", Toast.LENGTH_LONG).show()
            return
        }

        rgSeleccionado = findViewById(rgReporte!!.checkedRadioButtonId)

        when(rgSeleccionado?.tag.toString().toInt()){
            1 -> porcentajeFinalizacion()
            2 -> promedioCalificaciones()
            3 -> promedioValoraciones()
            else -> null
        }

    }

    private fun porcentajeFinalizacion(){
        lifecycleScope.launch {
            dialogTitulo.text = "Porcentaje de cursos finalizados entre ${tvFechaInicio.text} y ${tvFechaFinal.text}"
            var porc = reportesDao.obtenerPorcentajeFinalizado(tvFechaInicio.text.toString(), tvFechaFinal.text.toString())
            dialogDescripcion.text = "Porcentaje: ${porc}"
            dialog.show()
        }
    }

    private fun promedioCalificaciones(){
        lifecycleScope.launch {
            dialogTitulo.text = "Promedio de calificaciones entre ${tvFechaInicio.text} y ${tvFechaFinal.text}"
            var promedio = reportesDao.obtenerPromedioCalificacion(tvFechaInicio.text.toString(), tvFechaFinal.text.toString())
            dialogDescripcion.text = "Promedio: ${promedio}"
            dialog.show()
        }
    }

    private fun promedioValoraciones(){
        lifecycleScope.launch {
            dialogTitulo.text = "Promedio de valoraciones de cursos entre ${tvFechaInicio.text} y ${tvFechaFinal.text}"
            var promedio = reportesDao.obtenerPromedioValoraciones(tvFechaInicio.text.toString(), tvFechaFinal.text.toString())
            dialogDescripcion.text = "Promedio: ${promedio}"
            dialog.show()
        }
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_reporte
    }

}