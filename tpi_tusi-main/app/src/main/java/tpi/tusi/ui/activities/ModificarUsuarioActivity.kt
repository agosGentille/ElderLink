package tpi.tusi.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CiudadesDao
import tpi.tusi.data.daos.PaisesDao
import tpi.tusi.data.daos.ProvinciasDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.ui.entities.Ciudades
import tpi.tusi.ui.entities.Paises
import tpi.tusi.ui.entities.Provincias
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.daos.DireccionDao
import tpi.tusi.ui.dto.UsuarioDireccionDTO
import tpi.tusi.ui.entities.Direcciones
import tpi.tusi.ui.entities.Usuarios
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ModificarUsuarioActivity : BaseActivity() {

    private lateinit var sp: SharedPreferences
    private var usuarioId: Long? = 0L
    private var rolesUsuario: MutableSet<String>? = null
    private var usuarioEmail: String? = ""

    private lateinit var dni: TextView
    private lateinit var nombre: EditText
    private lateinit var apellido: EditText
    private lateinit var nacimiento: TextView
    private lateinit var nombreUsuario: EditText
    private lateinit var pais: Spinner
    private lateinit var prov: Spinner
    private lateinit var ciud: Spinner
    private lateinit var calle: EditText
    private lateinit var num: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnNac: Button

    private val paisesDao = PaisesDao()
    private val provinciasDao = ProvinciasDao()
    private val ciudadesDao = CiudadesDao()
    private val direccionDao = DireccionDao()
    private val usuariosDao = UsuariosDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = getSharedPreferences("usuario", Context.MODE_PRIVATE)
        usuarioId = sp.getLong("id", 0L)
        rolesUsuario = sp.getStringSet("roles", null)
        usuarioEmail = sp.getString("email", null)

        // Inicialización de vistas
        dni = findViewById(R.id.txtDni)
        nombre = findViewById(R.id.etNombre)
        apellido = findViewById(R.id.etApellido)
        nacimiento = findViewById(R.id.tvFechaNacimiento)
        nombreUsuario = findViewById(R.id.etNombreUsuario)
        pais = findViewById(R.id.spinner_pais)
        prov = findViewById(R.id.spinner_prov)
        ciud = findViewById(R.id.spinner_ciudad)
        calle = findViewById(R.id.etDireccionCalle)
        num = findViewById(R.id.etDireccionNro)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnNac = findViewById(R.id.button_fecha)

        // Cargar datos del usuario
        chargeUser(usuarioId!!)

        // Configuración de botones
        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        btnNac.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Configuración de validaciones de texto
        configurarValidacionesTexto()
    }

    private fun chargeUser(idUsuario: Long) {
        lifecycleScope.launch {
            val usuarioDto: UsuarioDireccionDTO? = usuariosDao.obtenerGestorConIDs(idUsuario)

            if (usuarioDto != null) {
                // Rellenar campos básicos
                dni.text = Editable.Factory.getInstance().newEditable(usuarioDto.dni)
                nombre.text = Editable.Factory.getInstance().newEditable(usuarioDto.nombre)
                apellido.text = Editable.Factory.getInstance().newEditable(usuarioDto.apellido)
                nombreUsuario.text = Editable.Factory.getInstance().newEditable(usuarioDto.nombreUsuario)
                calle.text = Editable.Factory.getInstance().newEditable(usuarioDto.calle)
                num.text = Editable.Factory.getInstance().newEditable(usuarioDto.numero.toString())

                // Formato de fecha
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val date = inputFormat.parse(usuarioDto.fechaNacimiento.toString())
                nacimiento.text = outputFormat.format(date!!)

                // Cargar datos de ubicación de forma secuencial
                withContext(Dispatchers.Main) {
                    // 1. Cargar países y seleccionar el correcto
                    val listaPaises = paisesDao.obtenerPaises()
                    val paisAdapter = ArrayAdapter(this@ModificarUsuarioActivity,
                        android.R.layout.simple_spinner_item, listaPaises)
                    paisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    pais.adapter = paisAdapter

                    // Seleccionar el país del usuario
                    for (i in 0 until pais.count) {
                        val paisItem = pais.getItemAtPosition(i) as Paises
                        if (paisItem.nombre.trim() == usuarioDto.pais.trim()) {
                            pais.setSelection(i)
                            break
                        }
                    }

                    // 2. Cargar provincias del país seleccionado
                    val paisSeleccionado = pais.selectedItem as Paises
                    val listaProvincias = provinciasDao.obtenerProvinciasPais(paisSeleccionado.id_pais)
                    val provAdapter = ArrayAdapter(this@ModificarUsuarioActivity,
                        android.R.layout.simple_spinner_item, listaProvincias)
                    provAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    prov.adapter = provAdapter

                    // Seleccionar la provincia del usuario
                    for (i in 0 until prov.count) {
                        val provinciaItem = prov.getItemAtPosition(i) as Provincias
                        if (provinciaItem.id_provincia == usuarioDto.idProvincia) {
                            prov.setSelection(i)
                            break
                        }
                    }

                    // 3. Cargar ciudades de la provincia seleccionada
                    val provinciaSeleccionada = prov.selectedItem as Provincias
                    val listaCiudades = ciudadesDao.obtenerCiudadesProvincia(provinciaSeleccionada.id_provincia)
                    val ciudadAdapter = ArrayAdapter(this@ModificarUsuarioActivity,
                        android.R.layout.simple_spinner_item, listaCiudades)
                    ciudadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    ciud.adapter = ciudadAdapter

                    // Seleccionar la ciudad del usuario
                    for (i in 0 until ciud.count) {
                        val ciudadItem = ciud.getItemAtPosition(i) as Ciudades
                        if (ciudadItem.id_ciudad == usuarioDto.idCiudad) {
                            ciud.setSelection(i)
                            break
                        }
                    }

                    // Configurar listeners DESPUÉS de cargar los datos iniciales
                    setupLocationListeners()
                }
            }
        }
    }

    private fun setupLocationListeners() {
        pais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (view != null) {  // Solo cargar si no es la selección inicial
                    val paisSeleccionado = parent.getItemAtPosition(position) as Paises
                    loadProvincias(paisSeleccionado.id_pais)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        prov.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (view != null) {  // Solo cargar si no es la selección inicial
                    val provinciaSeleccionada = parent.getItemAtPosition(position) as Provincias
                    loadCiudades(provinciaSeleccionada.id_provincia)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadProvincias(idPais: Long) {
        lifecycleScope.launch {
            val listaProvincias = provinciasDao.obtenerProvinciasPais(idPais)
            val adapter = ArrayAdapter(this@ModificarUsuarioActivity,
                android.R.layout.simple_spinner_item, listaProvincias)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            prov.adapter = adapter
        }
    }

    private fun loadCiudades(idProvincia: Long) {
        lifecycleScope.launch {
            val listaCiudades = ciudadesDao.obtenerCiudadesProvincia(idProvincia)
            val adapter = ArrayAdapter(this@ModificarUsuarioActivity,
                android.R.layout.simple_spinner_item, listaCiudades)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ciud.adapter = adapter
        }
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val fecha = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                nacimiento.text = fecha
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun configurarValidacionesTexto() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().any { !it.isLetter() }) {
                    val soloLetras = s.toString().filter { it.isLetter() }
                    when (currentFocus) {
                        apellido -> apellido.setText(soloLetras)
                        nombre -> nombre.setText(soloLetras)
                    }
                    currentFocus?.let {
                        if (it is EditText) {
                            it.setSelection(it.text.length)
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        apellido.addTextChangedListener(textWatcher)
        nombre.addTextChangedListener(textWatcher)
    }

    private fun guardarCambios() {
        if (!validarCamposNoVacios()) return

        lifecycleScope.launch {
            try {
                val ciudad = ciud.selectedItem as Ciudades
                val direccion = Direcciones(
                    id_direccion = 0,
                    calle = calle.text.toString(),
                    numero = num.text.toString().toInt(),
                    ciudad = ciudad.id_ciudad
                )

                // Manejo seguro de nullable
                val direccionId = withContext(Dispatchers.IO) {
                    direccionDao.getDirecPorCalleNumYCiud(direccion)?.id_direccion
                        ?: direccionDao.insertDireccion(direccion)
                } ?: run {
                    Toast.makeText(
                        this@ModificarUsuarioActivity,
                        "Error al crear la dirección",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val fechaNac = nacimiento.text.toString()
                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(fechaNac)
                val fechaNacimientoSqlDate = parsedDate?.let { Date(it.time) }

                val usuario = Usuarios(
                    id_usuario = usuarioId!!,
                    dni = dni.text.toString(),
                    nombre = nombre.text.toString(),
                    apellido = apellido.text.toString(),
                    fechaNacimiento = fechaNacimientoSqlDate!!,
                    email = usuarioEmail!!,
                    contraseña = "",
                    nombreUsuario = nombreUsuario.text.toString(),
                    direccion = direccionId
                )

                val resultado = withContext(Dispatchers.IO) {
                    usuariosDao.updateUsuario(usuario)
                }

                if (resultado) {
                    Toast.makeText(
                        this@ModificarUsuarioActivity,
                        "Usuario actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@ModificarUsuarioActivity, ActivityHome::class.java)
                    intent.putExtra("fragment", "mi_cuenta")
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@ModificarUsuarioActivity,
                        "Error al actualizar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ModificarUsuarioActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validarCamposNoVacios(): Boolean {
        val campos = listOf(
            dni to "DNI",
            nombre to "Nombre",
            apellido to "Apellido",
            nacimiento to "Fecha de Nacimiento",
            nombreUsuario to "Usuario",
            calle to "Calle",
            num to "Número"
        )

        for ((campo, nombreCampo) in campos) {
            if (campo.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    "El campo $nombreCampo es obligatorio.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return true
    }

    override fun obtenerPantalla(): Int {
        return R.layout.activity_modificar_cuenta
    }
}