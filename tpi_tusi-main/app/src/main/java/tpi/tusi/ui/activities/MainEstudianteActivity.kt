package tpi.tusi.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.ui.controller.PagerControllerEstudiante

class MainEstudianteActivity : BaseActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var sp: SharedPreferences

    private val cursosDao = CursosDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPager = findViewById(R.id.viewpagerEstudiante)
        tabLayout = findViewById(R.id.tablayoutEstudiante)

        // Configuracion del adaptador
        val pagerAdapter = PagerControllerEstudiante(this)
        viewPager.adapter = pagerAdapter

        // vinculacion entre TabLayout con el ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Cursos"
                1 -> "MisCursos"
                else -> "Cursos"
            }
        }.attach()
        sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)

        notificar()

    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_main_estudiante
    }

    private fun notificar(){
        val builder = AlertDialog.Builder(this)
        lifecycleScope.launch {
            val titleView = TextView(this@MainEstudianteActivity).apply {
                text = "Nuevos cursos!!"
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, 20, 0, 20)
            }

            val msj = getIntereses(sp.getLong("id", 0L))
            if (msj.isEmpty()) return@launch

            val msjs = msj.joinToString(separator = "\n")
            val messageView = TextView(this@MainEstudianteActivity).apply {
                text = msjs
                textSize = 16f
                setPadding(50, 20, 50, 20)
            }
            builder.setCustomTitle(titleView)
            builder.setView(messageView)
            builder.show()
        }
    }

    private suspend fun getIntereses(id: Long): ArrayList<String> {
        return cursosDao.getTitleCursoFromUserByIntereses(id)
    }

}