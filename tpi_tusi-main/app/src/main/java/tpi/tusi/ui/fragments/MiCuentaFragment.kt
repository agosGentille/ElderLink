package tpi.tusi.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CiudadesDao
import tpi.tusi.data.daos.DireccionDao
import tpi.tusi.data.daos.PaisesDao
import tpi.tusi.data.daos.ProvinciasDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.ui.activities.ModificarUsuarioActivity
import tpi.tusi.ui.entities.Ciudades
import tpi.tusi.ui.entities.Direcciones
import tpi.tusi.ui.entities.Paises
import tpi.tusi.ui.entities.Provincias
import tpi.tusi.ui.entities.Usuarios
import tpi.tusi.ui.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale

class MiCuentaFragment : Fragment(){

    private lateinit var sp: SharedPreferences
    private val userDao = UsuariosDao()
    private var usuarioId: Long? = 0L
    private var rolesUsuario: MutableSet<String>? = null
    private val direccionDao = DireccionDao()
    private val ciudadesDao = CiudadesDao()
    private val provinciasDao = ProvinciasDao()
    private val paisesDao = PaisesDao()
    private val dateUtils = DateUtils()

    private lateinit var dni: TextView
    private lateinit var nombre: TextView
    private lateinit var apellido: TextView
    private lateinit var edad: TextView
    private lateinit var fechaNac: TextView
    private lateinit var email: TextView
    private lateinit var username: TextView
    private lateinit var pais: TextView
    private lateinit var provincia: TextView
    private lateinit var ciudad: TextView
    private lateinit var direccion: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mi_cuenta, container, false)
        sp = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
        usuarioId = sp.getLong("id", 0L)
        rolesUsuario = sp.getStringSet("roles", null)
        var rol = 1
        if(rolesUsuario!!.contains("Estudiante")){
            rol = 3
        }else if(rolesUsuario!!.contains("Estudiante")){
            rol = 2
        }
        dni = view.findViewById(R.id.tvDni)
        nombre = view.findViewById(R.id.tvNombre)
        apellido = view.findViewById(R.id.tvApellido)
        edad = view.findViewById(R.id.tvEdad)
        fechaNac = view.findViewById(R.id.tvFechaNac)
        email = view.findViewById(R.id.tvCorreo)
        username = view.findViewById(R.id.tvNombreUsuario)
        pais = view.findViewById(R.id.tvPais)
        provincia = view.findViewById(R.id.tvProvincia)
        ciudad = view.findViewById(R.id.tvCiudad)
        direccion = view.findViewById(R.id.tvDireccion)

        chargeUser(sp.getString("email", null).toString())

        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        btnEdit.setOnClickListener {
            val intent = Intent(activity, ModificarUsuarioActivity::class.java)
            startActivity(intent)
        }
        /*
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this@MiCuentaFragment.requireContext())
                .setTitle("Confirmación de Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
                .setPositiveButton("Sí") { dialog, _ ->
                    lifecycleScope.launch {
                        userDao.deleteUsuario(usuarioId!!)
                        userDao.deleteRolUsuario(usuarioId!!, rol.toLong())
                        val intent = Intent(activity, AccesoActivity::class.java)
                        startActivity(intent)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }*/

        return view
    }

    private fun chargeUser(e: String) {
        lifecycleScope.launch {
            val usuario: Usuarios? = userDao.getUsuarioByEmail(e)
            if (usuario != null) {
                val address: Direcciones? = direccionDao.getDireccionById(usuario.direccion)
                val city: Ciudades? = ciudadesDao.getCityById(address!!.ciudad)
                val pcia: Provincias? = provinciasDao.getProvinciaById(city!!.provincia)
                val country: Paises? = paisesDao.getPaisById(pcia!!.pais)
                dni.text = usuario.dni
                nombre.text = usuario.nombre
                apellido.text = usuario.apellido
                edad.text = dateUtils.calculateAgeFromString(
                    usuario.fechaNacimiento.toString())
                    .toString()
                val fechaNacimiento = usuario.fechaNacimiento.toString()
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = inputFormat.parse(fechaNacimiento)
                val formattedDate = outputFormat.format(date!!)
                fechaNac.text = formattedDate.toString()
                email.text = usuario.email
                username.text = usuario.nombreUsuario
                pais.text = country?.nombre ?: "indefinido"
                provincia.text = pcia.nombre
                ciudad.text = city.nombre
                val calle = "${address.calle} ${address.numero}"
                direccion.text = calle
            }

        }
    }
}