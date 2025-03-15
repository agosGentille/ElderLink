package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.EtapasDao
import tpi.tusi.data.daos.EtiquetasDao
import com.bumptech.glide.Glide
import tpi.tusi.data.daos.AutoevaluacionesDao
import android.app.Dialog
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import kotlinx.coroutines.CoroutineScope
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.DenunciasDao
import tpi.tusi.data.daos.ValoracionesDao
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.adapters.VerEtapasAdapter
import tpi.tusi.ui.entities.Denuncias
import tpi.tusi.ui.entities.Email
import tpi.tusi.ui.entities.Etapas
import tpi.tusi.ui.entities.Valoraciones
import tpi.tusi.ui.utils.EmailUtils
import java.time.Instant

class vistaEspecificaCurso : BaseActivity() {
    private lateinit var nombre_curso: TextView
    private lateinit var descripcion_curso: TextView
    private lateinit var expandableListView: ExpandableListView
    private lateinit var portada: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var btnAutoevaluacion: Button
    private lateinit var btnVerIntentos: Button
    private lateinit var btnValorar: ImageButton
    private lateinit var btnDenunciar: ImageButton

    private var adapter: VerEtapasAdapter? = null
    private lateinit var listDataHeader: List<Etapas>
    private lateinit var listDataChild: HashMap<String, List<String>>
    private lateinit var sp: SharedPreferences

    private var cursoId: Long = 0L
    private var autoevalId: Long = 0L
    private var estudianteId: Long? = 0L
    private var rolesUsuario: MutableSet<String>? = null

    private val cDao = CursosDao()
    private val eDao = EtapasDao()
    private val etDao = EtiquetasDao()
    private val aDao = AutoevaluacionesDao()
    private val vDao = ValoracionesDao()
    private val dDao = DenunciasDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cursoId = intent.getLongExtra("curso_Id", -1L)
        if (cursoId == -1L ) {
            Toast.makeText(this, "Error al recibir el ID del curso", Toast.LENGTH_SHORT).show()
            finish()
        }
        sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
        estudianteId = sp.getLong("id", 0L)
        rolesUsuario = sp.getStringSet("roles", null)
        nombre_curso = findViewById(R.id.tv_nombre_curso)
        descripcion_curso = findViewById(R.id.tv_descripcion)
        expandableListView = findViewById(R.id.expandableListViewCursos)
        portada = findViewById(R.id.iv_Portada)
        chipGroup = findViewById(R.id.chipGroup)
        btnAutoevaluacion = findViewById(R.id.btnRealizarAutoeval)
        btnVerIntentos = findViewById(R.id.btnVerIntentos)
        lifecycleScope.launch {
            val autoevaluacion = aDao.obtenerAutoevaluacionPorCursoId(cursoId)
            if(autoevaluacion != null) {
                autoevalId = autoevaluacion.id_autoevaluacion
                btnAutoevaluacion.visibility = View.VISIBLE
                btnVerIntentos.visibility = View.VISIBLE
            }else{
                btnAutoevaluacion.visibility = View.GONE
                btnVerIntentos.visibility = View.GONE
            }
        }

        btnAutoevaluacion.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val etapasLeidas = eDao.validarEtapasLeidas(cursoId, estudianteId!!).count()
                val etapasTotales = eDao.getEtapasPorCurso(cursoId).count()
                Log.d("VistaEspecificaCurso", "Etapas Leidas: $etapasLeidas")
                Log.d("VistaEspecificaCurso", "Etapas Totales: $etapasTotales")
                if(etapasLeidas == etapasTotales){
                    val intent = Intent(this@vistaEspecificaCurso, RealizarAutoevaluacionAlumnoActivity::class.java)
                    intent.putExtra("cursoId", cursoId)
                    intent.putExtra("nombreCurso", nombre_curso.text.toString())
                    startActivity(intent)
                }else{
                    Toast.makeText(this@vistaEspecificaCurso, "Debe leer todas las etapas para realizar la autoevaluación", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnVerIntentos.setOnClickListener {
            val intent = Intent(this, NotaFinalAutoevaluacionActivity::class.java)
            intent.putExtra("autoevalId", autoevalId)
            intent.putExtra("usuarioId", estudianteId)
            startActivity(intent)
        }
        btnValorar = findViewById(R.id.btnAgregarValoracion)
        btnDenunciar = findViewById(R.id.btnDenunciarCurso)

        cargarDatosCurso()

        btnValorar.setOnClickListener{
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_valoraciones)

            val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
            val btnEnviarValoracion = dialog.findViewById<Button>(R.id.btnEnviarValoracion)

            btnEnviarValoracion.setOnClickListener {
                val valoracion = ratingBar.rating.toInt()
                if (cursoId != -1L && estudianteId != null) {
                    guardarValoracionEnBD(cursoId, estudianteId, valoracion)
                } else {
                    Toast.makeText(this, "Error: Estudiante no encontrado", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            dialog.show()
        }

        btnDenunciar.setOnClickListener{
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_denuncias)

            val btnEnviarDenuncia = dialog.findViewById<Button>(R.id.btnDenunciar)

            btnEnviarDenuncia.setOnClickListener {
                val razonDenuncia = dialog.findViewById<EditText>(R.id.detalle)
                if (cursoId != -1L && estudianteId != null) {
                    guardarDenunciaEnBD(cursoId, estudianteId, razonDenuncia.text.toString())
                    sendEmailToAdmin(
                        sp.getString("email", null).toString(),
                        nombre_curso.text.toString(),
                        razonDenuncia.text.toString()
                    )
                } else {
                    Toast.makeText(this, "Error: Estudiante no encontrado", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            dialog.show()
        }

        expandableListView.isNestedScrollingEnabled = true
        expandableListView.setOnGroupExpandListener {
            setExpandableListViewHeight(expandableListView)
        }
        expandableListView.setOnGroupCollapseListener {
            setExpandableListViewHeight(expandableListView)
        }

        setExpandableListViewHeight(expandableListView)
        bloquearControlesAdminGestor()

    }

    private fun bloquearControlesAdminGestor(){
        if (rolesUsuario!!.contains("estudiante")) {
            btnValorar.isEnabled = true
            btnVerIntentos.isEnabled = true
            adapter?.setCheckboxEnabled(true)
        } else{
            btnValorar.isEnabled = false
            btnVerIntentos.isEnabled = false
            adapter?.setCheckboxEnabled(false)
        }
    }

    private fun cargarDatosCurso() {
        lifecycleScope.launch {
            val curso = cDao.obtenerCursoEspecifico(cursoId)
            if(curso != null){
                nombre_curso.text = Editable.Factory.getInstance().newEditable(curso.nombre)
                descripcion_curso.text = Editable.Factory.getInstance().newEditable(curso.descripcion)

                listDataHeader = obtenerDataEtapas()
                listDataChild = listDataHeader.associate {
                    it.id_etapa.toString() to listOf(it.contenido)
                } as HashMap<String, List<String>>
                adapter = VerEtapasAdapter(
                    context = this@vistaEspecificaCurso,
                    listDataHeader = listDataHeader,
                    listDataChild = listDataChild,
                    eDao = eDao
                )

                expandableListView.setAdapter(adapter)

                val listEtiquetas = etDao.obtenerEtiquetasPorCurso(cursoId)
                chipGroup.removeAllViews()
                for (etiqueta in listEtiquetas) {
                    val chip = Chip(this@vistaEspecificaCurso)
                    chip.text = etiqueta.nombre
                    chip.setChipBackgroundColorResource(android.R.color.transparent)
                    chip.setTextColor(Color.BLUE)
                    chip.chipStrokeWidth = 2f
                    chipGroup.addView(chip)
                }
                if (!curso.thumbnailURL.isNullOrEmpty()) {
                    Glide.with(this@vistaEspecificaCurso)
                        .load(curso.thumbnailURL)
                        .placeholder(R.drawable.thumbnail)
                        .error(R.drawable.ic_quitar)
                        .into(portada)
                }
            }else{
                Toast.makeText(this@vistaEspecificaCurso, "No se pudo encontrar la información del curso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@vistaEspecificaCurso, MainEstudianteActivity::class.java)
                startActivity(intent)
            }
        }


    }

    fun guardarValoracionEnBD(idCurso: Long, idEstudiante: Long?, valoracion: Int) {
        if (idEstudiante != null) {
            val instant = Instant.now()
            val nuevaValoracion = Valoraciones(
                fk_curso = idCurso,
                fk_usuario = idEstudiante,
                valoracion = valoracion,
                fecha = instant
            )
            lifecycleScope.launch {
                if(vDao.insertValoracion(nuevaValoracion)){
                    Toast.makeText(this@vistaEspecificaCurso, "Valoración enviada exitosamente", Toast.LENGTH_SHORT).show()
                }else{Toast.makeText(this@vistaEspecificaCurso, "Error al guardar la valoración", Toast.LENGTH_SHORT).show()}
            }

        } else {
            Toast.makeText(this, "Error: Estudiante no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmailToAdmin(
        userEmail: String,
        nombreCurso: String,
        razon: String
    ) {
        val emailUtils = EmailUtils(
            DataDB.usernameGmail,
            DataDB.passwordGmail
        )
        val email = Email(
            recipient = DataDB.adminEmail,
            subject = "Denuncia de $userEmail al curso: $nombreCurso",
            message = razon
        )
        println(email)
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (emailUtils.sendEmail(email)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@vistaEspecificaCurso, "Denuncia enviada por email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@vistaEspecificaCurso, "Fallo el envio del correo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@vistaEspecificaCurso, "Ocurrio un fallo importante, revise el log", Toast.LENGTH_SHORT).show()
            println(e)
        }
    }

    fun guardarDenunciaEnBD(idCurso: Long, idEstudiante: Long?, razon: String) {
        if (idEstudiante != null) {
            val instant = Instant.now()
            val nuevaDenuncia = Denuncias(
                id_denuncia = 0,
                fk_curso = idCurso,
                fk_usuario = idEstudiante,
                fecha = instant,
                razon = razon
            )
            lifecycleScope.launch {
                if(dDao.insertDenuncia(nuevaDenuncia) != null){
                    Toast.makeText(this@vistaEspecificaCurso, "Reporte de Contenido enviado exitosamente", Toast.LENGTH_SHORT).show()
                }else{Toast.makeText(this@vistaEspecificaCurso, "Error al guardar la Denuncia", Toast.LENGTH_SHORT).show()}
            }

        } else {
            Toast.makeText(this, "Error: Estudiante no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun obtenerDataEtapas(): List<Etapas> {
        return withContext(Dispatchers.IO) {
            eDao.getEtapasPorCurso(cursoId) ?: emptyList()
        }
    }

    fun setExpandableListViewHeight(expandableListView: ExpandableListView) {
        val adapter = expandableListView.expandableListAdapter ?: return
        var totalHeight = 0
        for (i in 0 until adapter.groupCount) {
            val groupItem = adapter.getGroupView(i, false, null, expandableListView)
            groupItem.measure(0, 0)
            totalHeight += groupItem.measuredHeight

            if (expandableListView.isGroupExpanded(i)) {
                for (j in 0 until adapter.getChildrenCount(i)) {
                    val listItem = adapter.getChildView(i, j, false, null, expandableListView)
                    listItem.measure(0, 0)
                    totalHeight += listItem.measuredHeight
                }
            }
        }

        val params = expandableListView.layoutParams
        params.height = totalHeight + (expandableListView.dividerHeight * (adapter.groupCount - 1))
        expandableListView.layoutParams = params
        expandableListView.requestLayout()
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_vista_especifica_curso
    }
}