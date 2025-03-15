package tpi.tusi.ui.adapters

import tpi.tusi.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.EtapasDao
import tpi.tusi.ui.entities.Etapas
import tpi.tusi.ui.entities.UsuarioEtapa
import java.time.Instant


class VerEtapasAdapter(
    private val context: Context,
    private val listDataHeader: List<Etapas>,
    private val listDataChild: HashMap<String, List<String>>,
    private val eDao: EtapasDao
) : BaseExpandableListAdapter() {

    private var isCheckboxEnabled: Boolean = true

    fun setCheckboxEnabled(enabled: Boolean) {
        isCheckboxEnabled = enabled
        notifyDataSetChanged()
    }

    private fun getUserIdFromPreferences(): Long {
        val sharedPreferences = context.getSharedPreferences("usuario", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("id", -1)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        val etapaId = listDataHeader[groupPosition].id_etapa.toString()
        return listDataChild[etapaId]?.getOrNull(childPosition) ?: ""
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return listDataHeader[groupPosition].id_etapa
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.sub_item_expandable_list_etapas, parent, false)

        val txtContenido: TextView = view.findViewById(R.id.tvContenido)
        val youtubeWebView: WebView = view.findViewById(R.id.webViewVideo)
        val contenido = getChild(groupPosition, childPosition)

        // Expresión regular para detectar enlaces de YouTube y extraer el ID del video
        val youtubeRegex = """https?://(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/embed/)([^&\n?#]+)""".toRegex()
        val youtubeMatch = youtubeRegex.find(contenido)

        val spannableStringBuilder = SpannableStringBuilder(contenido)

        if (youtubeMatch != null) {
            // Si es un enlace de YouTube, extraemos el ID del video y configuramos el WebView
            val videoId = youtubeMatch.groupValues[1]
            val youtubeUrl = "https://www.youtube.com/embed/$videoId"

            youtubeWebView.visibility = View.VISIBLE
            youtubeWebView.settings.javaScriptEnabled = true
            youtubeWebView.settings.domStorageEnabled = true
            youtubeWebView.settings.loadWithOverviewMode = true
            youtubeWebView.settings.useWideViewPort = true
            youtubeWebView.loadUrl(youtubeUrl)

            // Ocultar el enlace de YouTube del texto visible
            spannableStringBuilder.delete(youtubeMatch.range.first, youtubeMatch.range.last + 1)
        } else {
            youtubeWebView.visibility = View.GONE
        }

        // Detecta todos los enlaces en el contenido y aplica ClickableSpan
        val linkRegex = """(https?://[^\s]+)""".toRegex()
        val matches = linkRegex.findAll(spannableStringBuilder)
        matches.forEach { matchResult ->
            val url = matchResult.value
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            spannableStringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        txtContenido.text = spannableStringBuilder
        txtContenido.movementMethod = LinkMovementMethod.getInstance()
        txtContenido.visibility = View.VISIBLE

        return view
    }


    override fun getChildrenCount(groupPosition: Int): Int {
        val etapaId = listDataHeader[groupPosition].id_etapa.toString()
        return listDataChild[etapaId]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Etapas {
        return listDataHeader[groupPosition]
    }

    override fun getGroupCount(): Int {
        return listDataHeader.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return listDataHeader[groupPosition].id_etapa
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_expandable_list_etapas, parent, false)

        val tituloEtapa: TextView = view.findViewById(R.id.tvTitulo)
        val checkBox: CheckBox = view.findViewById(R.id.cbLeido)
        val tvIdEtapa: TextView = view.findViewById(R.id.tvIdEtapa)

        val etapa = getGroup(groupPosition) as Etapas

        tituloEtapa.text = etapa.titulo
        tvIdEtapa.text = etapa.id_etapa.toString()
        val idUsuario = getUserIdFromPreferences()
        checkBox.isEnabled = isCheckboxEnabled

        CoroutineScope(Dispatchers.Main).launch {
            val etapaLeida = withContext(Dispatchers.IO) {
                eDao.getEtapaByUserAndId(etapa.id_etapa, idUsuario)
            }
            checkBox.isChecked = etapaLeida?.estado ?: false
            if (isCheckboxEnabled) {
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (etapaLeida == null) {
                            val nuevaEtapaUsuario = UsuarioEtapa(
                                fk_usuario = idUsuario,
                                fk_etapa = etapa.id_etapa,
                                estado = isChecked,
                                fecha = Instant.now()
                            )
                            eDao.insertEtapasXUsuario(nuevaEtapaUsuario)
                        } else {
                            eDao.updateEstadoYFechaEtapaLeida(etapa.id_etapa, idUsuario, isChecked)
                        }
                        val cDao = CursosDao()
                        cDao.agregarCursoAUsuario(idUsuario, etapa.fk_curso)
                    }
                }
            } else {
                checkBox.setOnCheckedChangeListener(null)
            }
        }

        return view
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}