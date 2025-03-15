package tpi.tusi.ui.activities
import android.content.Context
import android.content.Intent
import android.graphics.Color.WHITE
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import tpi.tusi.R

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(obtenerPantalla())

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.menu)
        toolbar = findViewById(R.id.barra_de_herramientas)

        setSupportActionBar(toolbar)

        setToolbarTitle("ElderLink")
        setToolbarColor(R.color.md_theme_light_onPrimaryContainer)

        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        setupNavigationDrawer()
        setupHeader()
        actionBarDrawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.md_theme_light_inverseOnSurface)
    }

    private fun setupNavigationDrawer() {
        val sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val roles = sp.getStringSet("roles", emptySet())
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    val sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
                    val r = sp.getStringSet("roles", null)
                    r?.let { rol ->
                        when {
                            "estudiante" in rol -> {
                                val intent = Intent(this, MainEstudianteActivity::class.java)
                                startActivity(intent)
                            }
                            "gestor" in rol -> {
                                val intent = Intent(this, MainGestorActivity::class.java)
                                startActivity(intent)
                            }
                            "admin" in rol -> {
                                val intent = Intent(this, MainAdminActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
                R.id.nav_mi_cuenta -> {
                    val intent = Intent(this, ActivityHome::class.java)
                    intent.putExtra("fragment", "mi_cuenta")
                    startActivity(intent)
                    true
                }
                R.id.nav_eventos -> {
                    val intent = Intent(this, ListadoEventosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cerrar_sesion -> {
                    val sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
                    val editor = sp.edit()
                    editor.clear()
                    editor.apply()
                    val intent = Intent(this, AccesoActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_denuncias -> {
                    if (roles!!.contains("admin")) {
                        val intent = Intent(this, DenunciasActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_reportes -> {
                    if (roles!!.contains("admin")) {
                        val intent = Intent(this, ReporteActivity::class.java)
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
            drawerLayout.closeDrawers()
            true
        }
        if (roles!!.contains("admin")) {
            navigationView.menu.findItem(R.id.nav_denuncias).isVisible = true
            navigationView.menu.findItem(R.id.nav_reportes).isVisible = true
        } else {
            navigationView.menu.findItem(R.id.nav_denuncias).isVisible = false
            navigationView.menu.findItem(R.id.nav_reportes).isVisible = false
        }
    }

    protected fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    // Método para configurar el color de la Toolbar
    protected fun setToolbarColor(colorResId: Int) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResId))
        toolbar.setTitleTextColor(WHITE)
    }

    private fun setupHeader() {
        val headerView = navigationView.getHeaderView(0)
        val nombreUsuarioTextView: TextView = headerView.findViewById(R.id.text_view_username)
        val correoUsuarioTextView: TextView = headerView.findViewById(R.id.text_view_email)
        val rolUsuarioTextView: TextView = headerView.findViewById(R.id.text_view_role)
        val sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
        val nombreUsuario = sp.getString("name", null)
        val apellidoUsuario = sp.getString("lastname", null)
        val correoUsuario = sp.getString("email", null)
        val nombreCompleto = if (!nombreUsuario.isNullOrEmpty() && !apellidoUsuario.isNullOrEmpty()) {
            "$apellidoUsuario, $nombreUsuario"
        } else {
            "Usuario no disponible"
        }
        val correo = correoUsuario ?: "Correo no disponible"

        val roles = sp.getStringSet("roles", emptySet())

        nombreUsuarioTextView.text = nombreCompleto
        correoUsuarioTextView.text = correo

        roles?.let { rol ->
            when {
                "estudiante" in rol -> {
                    rolUsuarioTextView.text = "Estudiante"
                }
                "gestor" in rol -> {
                    rolUsuarioTextView.text = "Gestor"
                }
                "admin" in rol -> {
                    rolUsuarioTextView.text = "Administrador"
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Método abstracto para que cada actividad defina su layout
    abstract fun obtenerPantalla(): Int
}