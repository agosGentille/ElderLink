package tpi.tusi.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.RolesDao
import tpi.tusi.data.daos.RolesUsuarioDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.data.database.EstadoUsuario
import tpi.tusi.ui.activities.GestionEstadoGestorActivity
import tpi.tusi.ui.activities.MainAdminActivity
import tpi.tusi.ui.activities.MainEstudianteActivity
import tpi.tusi.ui.activities.MainGestorActivity
import tpi.tusi.ui.activities.PasswordRecoveryActivity
import tpi.tusi.ui.config.PasswordUtils
import tpi.tusi.ui.entities.Usuarios

class LoginFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    private var rolSeleccionado: String? = null

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var recoveryBtn: Button
    private lateinit var imgBtnVerContra:ImageButton
    private lateinit var imgBtnOcultarContra:ImageButton
    private val userDao = UsuariosDao()
    private val rolesDao = RolesDao()
    private val rolesUsuarioDao = RolesUsuarioDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        email = view.findViewById(R.id.editText_email)
        password = view.findViewById(R.id.editText_contrasenia)
        loginBtn = view.findViewById(R.id.button_login)
        recoveryBtn = view.findViewById(R.id.btn_recuperar)
        imgBtnVerContra = view.findViewById(R.id.imageButton_verContra)
        imgBtnOcultarContra = view.findViewById(R.id.imageButton_ocultarContra)

        recoveryBtn.setOnClickListener {
            val intent = Intent(requireActivity(), PasswordRecoveryActivity::class.java)
            startActivity(intent)
        }
        imgBtnVerContra.setOnClickListener{
            password.inputType = InputType.TYPE_CLASS_TEXT;
            imgBtnVerContra.visibility = View.INVISIBLE
            imgBtnOcultarContra.visibility = View.VISIBLE
        }

        imgBtnOcultarContra.setOnClickListener{
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imgBtnVerContra.visibility = View.VISIBLE
            imgBtnOcultarContra.visibility = View.INVISIBLE
        }

        sharedPreferences = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        loginBtn.setOnClickListener {
            loginUser(email.text.toString(), password.text.toString())
        }
        return view
    }

    private fun loginUser(e: String, p: String){
        if (e.isNotEmpty()) {
            if(p.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = userDao.getUsuarioByEmail(e)
                    if (user != null) {
                        if (PasswordUtils.verifyPassword(p, user.contraseña)) {
                            Toast.makeText(
                                requireContext(),
                                "Ingreso correcto!",
                                Toast.LENGTH_SHORT
                            ).show()
                            saveUserToSharedPreferences(user)
                            val r = sharedPreferences.getStringSet("roles", null)
                            verificarCantidadRoles(r)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Email o contraseña incorrecta!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    } else {
                        Toast.makeText(requireContext(), "usuario inexistente!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }else{
                password.error = "Por favor, ingrese su Contraseña"
            }
        } else {
            email.error = "Por favor, ingrese su Correo Electrónico"
        }
    }

    private fun verificarCantidadRoles(roles: Set<String>?) {
        if (roles != null) {
            when (roles.size) {
                1 -> {
                    val rolUnico = roles.firstOrNull()
                    redirectToMainUnderRol(rolUnico.toString())
                }
                else -> {
                    seleccionRol(roles.toMutableSet())
                }
            }
        }
    }

    private fun redirectToMainUnderRol(rol: String) {
        val editando = sharedPreferences.edit()
        val newSet = mutableSetOf(rol)
        editando.putStringSet("roles", newSet)
        editando.apply()
        when (rol) {
            "estudiante" -> {
                val intent = Intent(requireActivity(), MainEstudianteActivity::class.java)
                startActivity(intent)
            }
            "gestor" -> {
                val intent = Intent(requireActivity(), MainGestorActivity::class.java)
                verificarCasoGestor(sharedPreferences.getLong("id", 0L)) { resultado ->
                    if (resultado) {
                        startActivity(intent)
                    } else {
                        mostrarDialogoYRedirigir()
                    }
                }
            }
            "admin" -> {
                val intent = Intent(requireActivity(), MainAdminActivity::class.java)
                startActivity(intent)
            }
            else -> {
                val fragment = RegistroPt1Fragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun mostrarDialogoYRedirigir() {
        val builder = AlertDialog.Builder(requireContext())
        val tituloCentrado = TextView(requireContext()).apply {
            text = "No autorizado!"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
            setTextColor(Color.BLACK)
        }

        builder.setCustomTitle(tituloCentrado)
        builder.setMessage("Aun no esta permitido el ingreso como Gestor, por favor intente nuevamente!!")
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        lifecycleScope.launch {
            delay(5000)
            dialog.dismiss()

            val fragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }


    private fun seleccionRol(roles: MutableSet<String>) {
        val rolesArray = roles.toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccione un ROL")
        var dialog: AlertDialog? = null
        builder.setSingleChoiceItems(rolesArray, -1) { _, which ->
            rolSeleccionado = rolesArray[which]
        }
        builder.setPositiveButton("Confirmar") { _, _ -> }
        dialog = builder.create()
        dialog.setOnShowListener {
            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                if (rolSeleccionado != null) {
                    redirectToMainUnderRol(rolSeleccionado!!)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Debe seleccionar un rol", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun verificarCasoGestor(id: Long, callback: (Boolean) -> Unit) {
        lifecycleScope.launch {
            val estado = withContext(Dispatchers.IO) {
                rolesUsuarioDao.getEstadoByRolGestorByUser(id)
            }

            val estadoEnum = EstadoUsuario.valueOf(estado)
            val posibleEstado = (estadoEnum == EstadoUsuario.aceptado)
            callback(posibleEstado)
        }
    }

    private suspend fun saveUserToSharedPreferences(user: Usuarios) {
        val sp = sharedPreferences.edit()
        sp.putLong("id", user.id_usuario)
        sp.putString("email", user.email)
        sp.putString("name", user.nombre)
        sp.putString("lastname", user.apellido)
        val roles = getRolesByUserId(user.id_usuario)
        sp.putStringSet("roles", roles.toSet())
        sp.apply()
        // Consola para ver datos guardados
//        viewSharedPreferencesOnConsole()
    }

    private suspend fun getRolesByUserId(id: Long): List<String?> {
        return rolesDao.getRolesByUserById(id)
    }

    private fun viewSharedPreferencesOnConsole(){
        Log.d("id usuario: ", sharedPreferences.getLong("id", 0L).toString())
        Log.d("email: ", sharedPreferences.getString("email", null).toString())
        Log.d("nombre: ", sharedPreferences.getString("name", null).toString())
        Log.d("apellido", sharedPreferences.getString("lastname", null).toString())
        //Log.d("roles: ", sharedPreferences.getString("roles", null).toString())
    }
}