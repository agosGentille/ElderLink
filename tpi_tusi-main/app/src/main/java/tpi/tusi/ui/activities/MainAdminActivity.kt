package tpi.tusi.ui.activities

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tpi.tusi.R
import tpi.tusi.ui.controller.PagerControllerAdmin
import tpi.tusi.ui.fragments.ListarCursosAdminFragment

class MainAdminActivity : BaseActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPager = findViewById(R.id.viewpagerAdmin)
        tabLayout = findViewById(R.id.tablayoutAdmin)

        // Configuracion del adaptador
        val pagerAdapter = PagerControllerAdmin(this)
        viewPager.adapter = pagerAdapter

        // vinculacion entre TabLayout con el ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Cursos"
                1 -> "Gestores"
                else -> "Cursos"
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        val fragment = supportFragmentManager.findFragmentByTag("f$position")
                        if (fragment is ListarCursosAdminFragment) {
                            fragment.recargarCursos()
                        }
                    }
                }
            }
        })

    }


    override fun obtenerPantalla(): Int {
        return R.layout.activity_main_admin
    }

}