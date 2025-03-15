package tpi.tusi.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpi.tusi.R
import tpi.tusi.data.daos.CursosDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Email
import tpi.tusi.ui.utils.EmailUtils

class GestionEstadoCursoActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var ivImagen: ImageView
    private lateinit var rgEstado: RadioGroup
    private lateinit var btnGuardar: Button
    private var rgSeleccionado: RadioButton? = null

    private var id_curso = 0L
    private var cursoDao = CursosDao()
    private var usuarioDao = UsuariosDao()

    private var idUsuarioCurso: Long = 0L
    private var tituloCurso:String = "NoEncontrado"
    private var nuevoEstado: Int = 0

    private val emailUtils = EmailUtils(DataDB.usernameGmail, DataDB.passwordGmail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_estado_curso)

        tvTitulo = findViewById(R.id.tvTitulo)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        ivImagen = findViewById(R.id.ivImagen)
        rgEstado = findViewById(R.id.rgEstado)
        btnGuardar = findViewById(R.id.btnSave)

        id_curso = intent.getLongExtra("id", 0L)

        btnGuardar.setOnClickListener {
            cambiarEstado()
        }

        cargarDatos()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val curso = cursoDao.obtenerCursoEspecifico(id_curso)
            tvTitulo.text = curso?.nombre ?: "Sin nombre"
            tvDescripcion.text = curso?.descripcion ?: "Sin descripción"
            Glide.with(this@GestionEstadoCursoActivity)
                .load(curso?.thumbnailURL) // URL de la imagen
                .placeholder(R.drawable.thumbnail) // Placeholder
                .error(R.drawable.tacho_imagen) // Imagen en caso de error
                .into(ivImagen)
            // Asignar el ID del usuario del curso a la variable idUsuarioCurso
            idUsuarioCurso = curso?.usuario ?: 0L
            tituloCurso = curso?.nombre ?: "Sin nombre"
        }
    }

    private fun cambiarEstado() {
        if (rgEstado.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Por favor, seleccione un estado antes de continuar", Toast.LENGTH_LONG).show()
            return
        }

        rgSeleccionado = findViewById(rgEstado.checkedRadioButtonId)

        lifecycleScope.launch {
            if (cursoDao.gestionarEstadoCurso(rgSeleccionado!!.tag.toString().toInt(), id_curso)) {
                Toast.makeText(this@GestionEstadoCursoActivity, "Se ha actualizado el estado exitosamente", Toast.LENGTH_LONG).show()
                nuevoEstado=rgSeleccionado!!.tag.toString().toInt()
                // Llamar a la función de envío de correo
                enviarCorreoConfirmacion(idUsuarioCurso, tituloCurso,nuevoEstado)//ACA DEVERIA INGRESAR EL IDUSUARIO

                val intent = Intent(this@GestionEstadoCursoActivity, MainAdminActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this@GestionEstadoCursoActivity, "Ha ocurrido un error al actualizar el estado", Toast.LENGTH_LONG).show()
            }

            finish()

        }
    }
    private fun enviarCorreoConfirmacion(idUsuario: Long, titulo:String,estado:Int) {
        lifecycleScope.launch {
            val usuario = usuarioDao.obtenerUsuarioById(idUsuario)
            val estadoCurso = when (estado) {
                1 -> "Aceptado"
                2 -> "Cancelado"
                3 -> "Pendiente"
                else -> "Desconocido" // Valor por defecto en caso de que no coincida con 1, 2 o 3
            }

            if (usuario != null) {
                val email = Email(
                    recipient = usuario.email,
                    subject = "Estado del curso actualizado",
                    message = "Estimado/a ${usuario.nombre} ${usuario.apellido},\n\n" +
                            "Le informamos que el estado de su curso, ${titulo}, ha sido ${estadoCurso} por un administrador, Revise su pestaña Mis Cursos."
                )
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (emailUtils.sendEmail(email)) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@GestionEstadoCursoActivity, "Denuncia enviada por email", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@GestionEstadoCursoActivity, "Fallo el envio del correo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GestionEstadoCursoActivity, "Ocurrio un fallo importante, revise el log", Toast.LENGTH_SHORT).show()
                    println(e)
                }
            } else {
                Toast.makeText(this@GestionEstadoCursoActivity, "No se encontró al usuario para el envío de correo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
