package tpi.tusi.ui.activities

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tpi.tusi.R
import tpi.tusi.R.id.viewpagerGestor
import tpi.tusi.ui.controller.PagerControllerGestor
import tpi.tusi.ui.fragments.ListarMisCursosGestorFragment

class MainGestorActivity : BaseActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPager = findViewById(viewpagerGestor)
        tabLayout = findViewById(R.id.tablayoutGestor)

        // Configuracion del adaptador
        val pagerAdapter = PagerControllerGestor(this)
        viewPager.adapter = pagerAdapter

        // vinculacion entre TabLayout con el ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Cursos"
                1 -> "MisCursos"
                else -> "Cursos"
            }
        }.attach()

    }
    //fun notificarCursoEditado() {
    //    // Buscar el fragmento de lista y notificarle
    //    supportFragmentManager.fragments.forEach { fragment ->
    //        if (fragment is ListarMisCursosGestorFragment) {
    //            fragment.CargarMisCusos()
    //        }
    //    }
    //}
    override fun obtenerPantalla(): Int {
        return R.layout.activity_main_gestor
    }
}