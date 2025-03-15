package tpi.tusi.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tpi.tusi.R
import tpi.tusi.ui.adapters.AccesoPageAdapter

class AccesoActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var accesoPageAdapter: AccesoPageAdapter
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager2 = findViewById(R.id.viewpager2)

        // Configuracion del adaptador
        accesoPageAdapter = AccesoPageAdapter(this)
        viewPager2.adapter = accesoPageAdapter
        fragmentContainer = findViewById(R.id.fragment_container)

        // vinculacion entre TabLayout con el ViewPager2
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Iniciar sesión"
                1 -> "Registrarse"
                else -> null
            }
        }.attach()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager2.currentItem == 0) {
                    mostrarDialogoSalir()
                }else if(viewPager2.currentItem == 1){
                        viewPager2.currentItem = 0
                }
            }
        })
    }


private fun mostrarDialogoSalir() {
    val builder = AlertDialog.Builder(this)
    builder.setMessage("¿Seguro que quieres salir de la aplicación?")
        .setCancelable(false)
        .setPositiveButton("Sí") { dialog, id ->
            finishAffinity()
        }
        .setNegativeButton("No") { dialog, id ->
            dialog.dismiss()
        }
    val alert = builder.create()
    alert.show()
}

fun mostrarFragmentContainer() {
    // Oculta el ViewPager2 y el tab y muestra el FragmentContainer
    viewPager2.visibility = View.GONE
    tabLayout.visibility = View.GONE
    fragmentContainer.visibility = View.VISIBLE
}

fun ocultarFragmentContainer() {
    // Oculta el FragmentContainer y muestra el ViewPager2 con el tab
    fragmentContainer.visibility = View.GONE
    viewPager2.visibility = View.VISIBLE
    tabLayout.visibility = View.VISIBLE
}

fun registroFinalizado() {
    viewPager2.setCurrentItem(0, true)
}

}