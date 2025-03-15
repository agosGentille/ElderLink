package tpi.tusi.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.ui.dto.UsuarioDTO

class GestionEstadoGestorActivity : BaseActivity() {

    private lateinit var tvDni: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvNac:TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvPais: TextView
    private lateinit var tvProvincia: TextView
    private lateinit var tvCiudad: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvNombreUsuario: TextView
    private var rgEstado: RadioGroup? = null
    private var rgSeleccionado: RadioButton? = null
    private lateinit var btnGuardar:Button

    private val usuariosDao = UsuariosDao()
    private var usuarioDto = UsuarioDTO()

    private var id_usuario: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvDni = findViewById(R.id.textView_dni)
        tvNombre = findViewById(R.id.textView_nombre)
        tvApellido = findViewById(R.id.textView_apellido)
        tvNac = findViewById(R.id.textView_nac)
        tvCorreo = findViewById(R.id.textView_correo)
        tvPais = findViewById(R.id.textView_pais)
        tvProvincia = findViewById(R.id.textView_provincia)
        tvCiudad = findViewById(R.id.textView_ciudad)
        tvDireccion = findViewById(R.id.textView_direccion)
        tvNombreUsuario = findViewById(R.id.textView_usuario)
        rgEstado = findViewById(R.id.rgStatus)
        btnGuardar = findViewById(R.id.btnSave)

        id_usuario = intent.getLongExtra("id", -1L)

        btnGuardar.setOnClickListener {
            guardar()
        }

        cargarDatos()
    }

    private fun cargarDatos(){
        lifecycleScope.launch {
            usuarioDto = usuariosDao.obtenerGestor(id_usuario)

            tvDni.setText("DNI: " + usuarioDto.dni)
            tvNombre.setText("Nombre: " + usuarioDto.nombre)
            tvApellido.setText("Apellido: " + usuarioDto.apellido)
            tvNac.setText("Fecha de nacimiento: " + usuarioDto.fechaNacimiento)
            tvCorreo.setText("Correo: " + usuarioDto.email)
            tvPais.setText("País: " + usuarioDto.pais)
            tvCiudad.setText("Ciudad: " + usuarioDto.ciudad)
            tvProvincia.setText("Provincia: " + usuarioDto.provincia)
            tvDireccion.setText("Dirección: " + usuarioDto.calle + " " + usuarioDto.numero)
            tvNombreUsuario.setText("Nombre de usuario: " + usuarioDto.nombreUsuario)

        }
    }

    private fun guardar() {

        if (rgEstado?.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Por favor, seleccione un estado antes de continuar", Toast.LENGTH_LONG).show()
            return
        }

        rgSeleccionado = findViewById(rgEstado!!.checkedRadioButtonId)

        lifecycleScope.launch {

            if(usuariosDao.gestionarEstadoGestor(rgSeleccionado!!.tag.toString().toInt(), id_usuario)) {
                Toast.makeText(this@GestionEstadoGestorActivity,"Se ha actualizado el estado exitosamente", Toast.LENGTH_LONG).show()
                val intent = Intent(this@GestionEstadoGestorActivity, MainAdminActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this@GestionEstadoGestorActivity,"Ha ocurrido un error al actualizar el estado", Toast.LENGTH_LONG).show()
            }

        }

    }

    override fun obtenerPantalla(): Int {
        return R.layout.fragment_gestion_estado_gestor
    }
}