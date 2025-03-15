package tpi.tusi.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.CiudadesDao
import tpi.tusi.data.daos.PaisesDao
import tpi.tusi.data.daos.ProvinciasDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.ui.activities.AccesoActivity
import tpi.tusi.ui.entities.Ciudades
import tpi.tusi.ui.entities.Paises
import tpi.tusi.ui.entities.Provincias
import java.util.Calendar

class RegistroPt1Fragment : Fragment() {

    private lateinit var etDni:EditText
    private lateinit var etNombre:EditText
    private lateinit var etApellido:EditText
    private lateinit var btnNac:Button
    private lateinit var tvNac:TextView
    private lateinit var etCorreo:EditText
    private lateinit var etContrasenia:EditText
    private lateinit var etRepetirContrasenia:EditText
    private lateinit var spPais:Spinner
    private lateinit var spProvincia:Spinner
    private lateinit var spCiudad:Spinner
    private lateinit var etCalle:EditText
    private lateinit var etNumero:EditText
    private lateinit var btnSiguiente:Button
    private lateinit var etUsuario:EditText
    private lateinit var imgBtnVerContra:ImageButton
    private lateinit var imgBtnOcultarContra:ImageButton
    private lateinit var imgRepOcultar: ImageButton
    private lateinit var imgRepVer: ImageButton

    private val paisesDao = PaisesDao()
    private val provinciasDao = ProvinciasDao()
    private val ciudadesDao = CiudadesDao()
    private val usuariosDao = UsuariosDao()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registro_pt1, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Inicializamos variables
        etDni = view.findViewById(R.id.editText_dni)
        etNombre = view.findViewById(R.id.editText_nombre)
        etApellido = view.findViewById(R.id.editText_apellido)
        btnNac = view.findViewById(R.id.buttom_fecha)
        tvNac = view.findViewById(R.id.textView_fechaNac)
        etCorreo = view.findViewById(R.id.editText_email)
        etUsuario = view.findViewById(R.id.editText_usuario)
        etContrasenia = view.findViewById(R.id.editText_contrasenia)
        etRepetirContrasenia = view.findViewById(R.id.editText_repetir_contrasenia)
        spPais = view.findViewById(R.id.spinner_pais)
        spProvincia = view.findViewById(R.id.spinner_provincia)
        spCiudad = view.findViewById(R.id.spinner_ciudad)
        etCalle = view.findViewById(R.id.editText_calle)
        etNumero = view.findViewById(R.id.editText_numero)
        btnSiguiente = view.findViewById(R.id.button_registroPt2)
        imgBtnVerContra = view.findViewById(R.id.imageButton_verContra)
        imgBtnOcultarContra = view.findViewById(R.id.imageButton_ocultarContra)
        imgRepVer = view.findViewById(R.id.imageButton_rep_verContra)
        imgRepOcultar = view.findViewById(R.id.imageButton_rep_ocultarContra)

        //Cargamos el spinner de Países
        loadPaises()

        //Calendario al seleccionar el EditText de fecha
        btnNac.setOnClickListener {
            // Obtiene la fecha actual como predeterminada para el selector
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Muestra el DatePickerDialog
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Formato de fecha (puedes ajustarlo según tu preferencia)
                val fecha = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                tvNac.text = fecha
            }, year, month, day)

            datePickerDialog.show()
        }

        //Botón siguiente
        btnSiguiente.setOnClickListener {
            if(validarCamposNoVacios() && validarRepContraseña()) {
                siguiente()
            }
        }

        //Se cargan las provincias en el spinner de spProvincias según el país seleccionado
        spPais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val paisSelect = parent.getItemAtPosition(position) as Paises
                loadProvincias(paisSelect.id_pais)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        //Se cargan las ciudades en el spinner de spCiudades según la provincia seleccionada
        spProvincia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val provinciaSelect = parent.getItemAtPosition(position) as Provincias
                loadCiudades(provinciaSelect.id_provincia)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        imgBtnVerContra.setOnClickListener{
            etContrasenia.inputType = InputType.TYPE_CLASS_TEXT;
            imgBtnVerContra.visibility = View.INVISIBLE
            imgBtnOcultarContra.visibility = View.VISIBLE
        }

        imgBtnOcultarContra.setOnClickListener{
            etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imgBtnVerContra.visibility = View.VISIBLE
            imgBtnOcultarContra.visibility = View.INVISIBLE
        }

        imgRepVer.setOnClickListener{
            etRepetirContrasenia.inputType = InputType.TYPE_CLASS_TEXT;
            imgRepVer.visibility = View.INVISIBLE
            imgRepOcultar.visibility = View.VISIBLE
        }

        imgRepOcultar.setOnClickListener{
            etRepetirContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imgRepVer.visibility = View.VISIBLE
            imgRepOcultar.visibility = View.INVISIBLE
        }

        //Validación solo letras
        etNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().any { !it.isLetter() }) {
                    etNombre.setText(s.toString().filter { it.isLetter() })
                    etNombre.setSelection(etNombre.text.length) // Mueve el cursor al final
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //Validación solo letras
        etApellido.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().any { !it.isLetter() }) {
                    etApellido.setText(s.toString().filter { it.isLetter() })
                    etApellido.setSelection(etApellido.text.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //Validación formato mail
        etCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                if (s.toString().isNotEmpty() && !s.toString().matches(emailPattern.toRegex())) {
                    etCorreo.error = "Formato de correo inválido"
                } else {
                    etCorreo.error = null // Limpia el error si es válido
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    override fun onResume() {
        super.onResume()
        (activity as? AccesoActivity)?.ocultarFragmentContainer()
    }

    private fun loadPaises() {
        viewLifecycleOwner.lifecycleScope.launch {
            val listaPaises = paisesDao.obtenerPaises()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaPaises)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spPais.adapter = adapter

            adapter.notifyDataSetChanged()

            val paisSeleccionado = spPais.selectedItem as Paises
            loadProvincias(paisSeleccionado.id_pais)

        }
    }

    private fun loadProvincias(idPais: Long){
        viewLifecycleOwner.lifecycleScope.launch {
            val listaProvincias = provinciasDao.obtenerProvinciasPais(idPais)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaProvincias)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProvincia.adapter = adapter

            adapter.notifyDataSetChanged()

            val provinciaSeleccionada = spProvincia.selectedItem as Provincias
            loadCiudades(provinciaSeleccionada.id_provincia)

        }
    }

    private fun loadCiudades(idProvincia: Long){
        viewLifecycleOwner.lifecycleScope.launch {
            val listaCiudades = ciudadesDao.obtenerCiudadesProvincia(idProvincia)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaCiudades)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCiudad.adapter = adapter

            adapter.notifyDataSetChanged()

        }
    }

    private fun validarCamposNoVacios(): Boolean {
        val campos = listOf(
            etDni to "DNI",
            etNombre to "Nombre",
            etApellido to "Apellido",
            tvNac to "Fecha de Nacimiento",
            etCorreo to "Correo Electrónico",
            etUsuario to "Usuario",
            etContrasenia to "Contraseña",
            etRepetirContrasenia to "Repetir contraseña",
            etCalle to "Calle",
            etNumero to "Número"
        )

        for ((campo, nombreCampo) in campos) {
            if (campo.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "El campo $nombreCampo es obligatorio.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun validarRepContraseña(): Boolean{
        if(etRepetirContrasenia.text.toString().equals(etContrasenia.text.toString()))
            return true
        return false
    }

    private suspend fun validacionRepiteMail(): Boolean {
        return usuariosDao.validarMail(etCorreo.text.toString())
    }

    private fun validacionFormatoMail(): Boolean{
        val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return etCorreo.text.toString().matches(Regex(emailRegex))
    }

    private fun siguiente() {

        viewLifecycleOwner.lifecycleScope.launch {
            if (!validacionFormatoMail()) {
                Toast.makeText(context, "Formato de mail inválido", Toast.LENGTH_LONG).show()
                return@launch
            }

            // Llama a la validación en la coroutine
            if (validacionRepiteMail()) {
                Toast.makeText(
                    context,
                    "El mail ingresado ya existe. Pruebe con uno distinto",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val dni = etDni.text.toString()
            val nombre = etNombre.text.toString()
            val apellido = etApellido.text.toString()
            val nacimiento = tvNac.text.toString()
            val correo = etCorreo.text.toString()
            val usuario = etUsuario.text.toString()
            val contrasenia = etContrasenia.text.toString()
            val ciudad = spCiudad.selectedItem as Ciudades
            val calle = etCalle.text.toString()
            val numero = etNumero.text.toString().toInt()

            val bundle = Bundle().apply {
                putString("dni", dni)
                putString("nombre", nombre)
                putString("apellido", apellido)
                putString("nacimiento", nacimiento)
                putString("correo", correo)
                putString("usuario", usuario)
                putString("contrasenia", contrasenia)
                putLong("ciudad", ciudad.id_ciudad)
                putString("calle", calle)
                putInt("numero", numero)
            }

            // Cargar el siguiente fragmento
            val registroPt2Fragment = RegistroPt2Fragment().apply {
                arguments = bundle
            }

            // Reemplazar el fragmento actual
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, registroPt2Fragment)
                .addToBackStack(null)
                .commit()
        }
    }

}