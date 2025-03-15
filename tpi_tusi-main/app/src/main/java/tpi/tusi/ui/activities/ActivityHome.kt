package tpi.tusi.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import tpi.tusi.R
import tpi.tusi.ui.fragments.HomeFragment
import tpi.tusi.ui.fragments.LoginFragment
import tpi.tusi.ui.fragments.MiCuentaFragment

class ActivityHome : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentToLoad = intent.getStringExtra("fragment")

        // Cargamos el fragmento correspondiente
        loadFragment(when (fragmentToLoad) {
            "mi_cuenta" -> MiCuentaFragment()
            "home" -> HomeFragment()
            else -> LoginFragment()
        })
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_home
    }
}