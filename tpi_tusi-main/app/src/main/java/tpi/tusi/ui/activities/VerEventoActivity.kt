package tpi.tusi.ui.activities

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.data.daos.EventosDao

class VerEventoActivity : BaseActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tableLayoutEtiquetas: TableLayout
    private lateinit var imageView: ImageView
    private lateinit var tvFecha: TextView

    private var id: Long = 0L

    private val etiquetasDao = EtiquetasDao()
    private val eventosDao = EventosDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getLongExtra("evento", 1L)

        tvTitulo = findViewById(R.id.textView_titulo_evento)
        tvDescripcion = findViewById(R.id.textView_descripcion_evento)
        tableLayoutEtiquetas = findViewById(R.id.tableLayout_etiquetas)
        imageView = findViewById(R.id.imageView_evento)
        tvFecha = findViewById(R.id.textView_fecha_evento)

        setearDatos(id)

    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_ver_evento
    }

    private fun cargarEtiquetas() {
        lifecycleScope.launch {
            val etiquetas = etiquetasDao.obtenerEtiquetasEvento(id)
            var tableRow: TableRow? = null
            var checkBoxCount = 0

            for (etiqueta in etiquetas) {
                if (checkBoxCount % 2 == 0) {
                    tableRow = TableRow(this@VerEventoActivity)
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                val checkBox = CheckBox(this@VerEventoActivity).apply {
                    text = etiqueta.nombre
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    tag = etiqueta.id_etiqueta
                }

                checkBox.isChecked = true
                checkBox.isEnabled = false

                tableRow?.addView(checkBox)

                checkBoxCount++

                if (checkBoxCount % 2 == 0) {
                    tableLayoutEtiquetas.addView(tableRow)
                }
            }

            if (checkBoxCount % 2 != 0 && tableRow != null) {
                tableLayoutEtiquetas.addView(tableRow)
            }
        }
    }


    private fun setearDatos(idEvento:Long){
        lifecycleScope.launch {
            val evento = eventosDao.traerEvento(idEvento)
            if(evento!=null) {
                tvTitulo.text = evento.titulo
                tvDescripcion.text = evento.descripcion
                tvFecha.text = evento.fecha.toString()
                //Setea imagen
                Glide.with(this@VerEventoActivity)
                    .load(evento.url_imagen)
                    .placeholder(R.drawable.thumbnail)
                    .error(R.drawable.tacho_imagen)
                    .into(imageView)

                cargarEtiquetas()
            }
        }
    }

}