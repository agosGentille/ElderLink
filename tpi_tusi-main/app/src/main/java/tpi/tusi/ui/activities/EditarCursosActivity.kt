package tpi.tusi.ui.activities


import android.os.Bundle
import tpi.tusi.R
import androidx.fragment.app.Fragment
import tpi.tusi.ui.fragments.CrearCursoFragment
import tpi.tusi.ui.fragments.EditarCursoFragment

class EditarCursosActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(obtenerPantalla())

        // Obtener datos del curso desde el Intent
        val cursoId = intent.getLongExtra("curso_id", -1)
        val cursoNombre = intent.getStringExtra("curso_nombre")
        val cursoDescripcion = intent.getStringExtra("curso_descripcion")
        val cursoThumbnail = intent.getStringExtra("curso_thumbnail")
        val cursoEstado = intent.getLongExtra("curso_estado", 0)


        val fragment = EditarCursoFragment().apply {
            arguments = Bundle().apply {
                putLong("curso_id", cursoId)
                putString("curso_nombre", cursoNombre)
                putString("curso_descripcion", cursoDescripcion)
                putString("curso_thumbnail", cursoThumbnail)
                putLong("curso_estado", cursoEstado)
                putBoolean("modo_edicion",true)
            }
            //Pasar el listener al Fragment
            //this.cursoEditadoListener = this@EditarCursosActivity.cursoEditadoListener
        }
        // Cargar el fragmento con los datos proporcionados
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.contenedor, fragment)
        fragmentTransaction.commit()
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_editar_cursos
    }

}