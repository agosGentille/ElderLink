package tpi.tusi.ui.activities

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.PasswordRecoveryDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.config.PasswordUtils
import tpi.tusi.ui.entities.Email
import tpi.tusi.ui.entities.Usuarios
import tpi.tusi.ui.utils.EmailUtils

class PasswordRecoveryActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var enviarCodeBtn: Button
    private lateinit var codigoInput: EditText
    private lateinit var verificarCodeBtn: Button
    private lateinit var passInput: EditText
    private lateinit var pass2Input: EditText
    private lateinit var sendNewPassBtn: Button
    private lateinit var imgBtnVerContra: ImageButton
    private lateinit var imgBtnOcultarContra: ImageButton
    private lateinit var imgBtnVerContra2:ImageButton
    private lateinit var imgBtnOcultarContra2:ImageButton

    private var user: Usuarios? = null

    private val usuarioDao = UsuariosDao()
    private val passwordRecoveryDao = PasswordRecoveryDao()
    private val passwordUtils = PasswordUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contra)

        emailInput = findViewById(R.id.email)
        enviarCodeBtn = findViewById(R.id.sendCode)
        codigoInput = findViewById(R.id.code)
        verificarCodeBtn = findViewById(R.id.validateCode)
        passInput = findViewById(R.id.pass)
        pass2Input = findViewById(R.id.repass)
        sendNewPassBtn = findViewById(R.id.newPass)

        codigoInput.visibility = View.GONE
        verificarCodeBtn.visibility = View.INVISIBLE
        passInput.visibility = View.GONE
        pass2Input.visibility = View.GONE
        sendNewPassBtn.visibility = View.INVISIBLE

        imgBtnVerContra = findViewById(R.id.imageButton_verContra)
        imgBtnOcultarContra = findViewById(R.id.imageButton_ocultarContra)

        imgBtnVerContra2 = findViewById(R.id.imageButton_verContraRepass)
        imgBtnOcultarContra2 = findViewById(R.id.imageButton_ocultarContraRepass)

        imgBtnVerContra.visibility = View.GONE
        imgBtnVerContra2.visibility = View.GONE
        imgBtnOcultarContra.visibility = View.GONE
        imgBtnOcultarContra2.visibility = View.GONE

        enviarCodeBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                verificarEmailExiste(emailInput.text.toString())
            }
        }
        verificarCodeBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                verificarCodeIngresado(codigoInput.text.toString())
            }
        }

        sendNewPassBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                verificarPasswordSendToDb(passInput.text.toString(), pass2Input.text.toString())
            }
        }

        imgBtnVerContra.setOnClickListener{
            passInput.inputType = InputType.TYPE_CLASS_TEXT;
            imgBtnVerContra.visibility = View.INVISIBLE
            imgBtnOcultarContra.visibility = View.VISIBLE
        }

        imgBtnOcultarContra.setOnClickListener{
            passInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imgBtnVerContra.visibility = View.VISIBLE
            imgBtnOcultarContra.visibility = View.INVISIBLE
        }

        imgBtnVerContra2.setOnClickListener{
            pass2Input.inputType = InputType.TYPE_CLASS_TEXT;
            imgBtnVerContra.visibility = View.INVISIBLE
            imgBtnOcultarContra.visibility = View.VISIBLE
        }

        imgBtnOcultarContra2.setOnClickListener{
            pass2Input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imgBtnVerContra.visibility = View.VISIBLE
            imgBtnOcultarContra.visibility = View.INVISIBLE
        }
    }

    private suspend fun verificarPasswordSendToDb(p1: String, p2: String) {
        if (
            (p1 == p2) &&
            passwordUtils.validandoPassword(p1) &&
            passwordUtils.validandoPassword(p2)
            ) {
            val newPassword = passwordUtils.hashPassword(p1)
                val resultado = usuarioDao.updatePasswordUsuario(user!!.id_usuario, newPassword)
                if (resultado) {
                    Toast.makeText(this, "Su contraseña a sido cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    passwordRecoveryDao.deleteRowFromDB(user!!.id_usuario)
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(this, "Ocurrio un error, por favor intente nuevamente", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Las contraseñas no coinciden!", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun verificarCodeIngresado(code: String) {
        val codigo = passwordRecoveryDao.getCodeByUserId(user!!.id_usuario)
        if (codigo.toString() == code) {
            passInput.visibility = View.VISIBLE
            pass2Input.visibility = View.VISIBLE
            sendNewPassBtn.visibility = View.VISIBLE
            imgBtnVerContra.visibility = View.VISIBLE
            imgBtnVerContra2.visibility = View.VISIBLE
            Toast.makeText(this, "Codigo correcto!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El codigo ingresado no es valido", Toast.LENGTH_SHORT).show()
            codigoInput.text.clear()
        }
    }

    private suspend fun verificarEmailExiste(email: String) {

        if (esEmailValido(email)) {
            val emailExist = usuarioDao.getIfUserExistByEmail(email)
            if (emailExist) {
                Toast.makeText(this, "Se ha enviado el codigo a su correo", Toast.LENGTH_SHORT ).show()
                generarCodeAndSaveOnDB(emailInput.text.toString())
                viewCodeCard()
            } else {
                Toast.makeText(this, "Correo inexistente", Toast.LENGTH_SHORT).show()
                emailInput.text.clear()
            }
        } else {
            Toast.makeText(this, "Debe ingresar un correo valido", Toast.LENGTH_SHORT).show()
        }

    }
    private fun esEmailValido(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        return Regex(regex).matches(email)
    }
    private fun viewCodeCard(){
        codigoInput.visibility = View.VISIBLE
        verificarCodeBtn.visibility = View.VISIBLE
    }

    private fun generarCodeAndSaveOnDB(email: String) {
        val code = (100000..999999).random()
        lifecycleScope.launch {
            user = usuarioDao.getUsuarioByEmail(email)
            if (user != null) {
                passwordRecoveryDao.saveUserAndCode(user!!.id_usuario, code)
                sendEmailWithCodeToUser(user!!.email, code)
            } else {
                println("no entrro")
            }
        }

    }
    private fun sendEmailWithCodeToUser(e: String, code: Int) {
        val emailUtils = EmailUtils(
            DataDB.usernameGmail,
            DataDB.passwordGmail
        )
        val email = Email(
            recipient = e,
            subject = "ElderLink, mensaje importante",
            message = "Su codigo de recuperacion de contraseña es $code\n"
        )
        println(email)
        try {
            CoroutineScope(Dispatchers.IO).launch {
                emailUtils.sendEmail(email)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ocurrio un fallo importante, revise el log", Toast.LENGTH_SHORT).show()
            println(e)
        }
    }

}