package tpi.tusi.ui.activities


import android.os.Bundle
import tpi.tusi.R
import androidx.fragment.app.Fragment
import tpi.tusi.ui.fragments.CrearCursoFragment

class CrearCursosActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadFragment(CrearCursoFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contenedor, fragment)
        fragmentTransaction.commit()
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_crear_cursos
    }

}