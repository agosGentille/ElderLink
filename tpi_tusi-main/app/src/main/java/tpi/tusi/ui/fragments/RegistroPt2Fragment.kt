package tpi.tusi.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.EtiquetasDao
import tpi.tusi.data.daos.UsuariosDao
import tpi.tusi.ui.activities.AccesoActivity
import tpi.tusi.ui.config.PasswordUtils
import tpi.tusi.ui.entities.Direcciones
import tpi.tusi.ui.entities.Etiquetas
import tpi.tusi.ui.entities.Usuarios
import java.text.SimpleDateFormat
import java.sql.Date
import java.util.Locale

class RegistroPt2Fragment : Fragment() {

    //Controles de esta pantalla
    private lateinit var tableLayout: TableLayout
    private lateinit var checkbox_alumno: CheckBox
    private lateinit var checkbox_gestor: CheckBox
    private lateinit var botonFinalizar: Button

    private val etiquetasDao = EtiquetasDao()
    private val usuariosDao = UsuariosDao()

    private val selectedEtiquetasIds = mutableListOf<Long>()
    private val selectedRolesIds = mutableListOf<Long>()

    var dni: String = ""
    var nombre: String = ""
    var apellido: String = ""
    var nacimiento: Date? = Date(System.currentTimeMillis())
    var correo: String = ""
    var usuario: String = ""
    var contrasenia: String = ""
    var ciudad: Long = 0L
    var calle: String = ""
    var numero: Int = 0

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registro_pt2, container, false)
        botonFinalizar = view.findViewById(R.id.button_finalizar)
        tableLayout = view.findViewById(R.id.tableLayout_etiquetas)
        checkbox_gestor = view.findViewById(R.id.checkBox_gestor)
        checkbox_alumno = view.findViewById(R.id.checkBox_alumno)

        //Carga los checkbox de los intereses
        cargarEtiquetas()

        botonFinalizar.setOnClickListener{
            registrarse()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let {
            dni = it.getString("dni")!!
            nombre = it.getString("nombre")!!
            apellido = it.getString("apellido")!!
            nacimiento = Date(formato.parse(it.getString("nacimiento")!!)!!.time)
            correo = it.getString("correo")!!
            usuario = it.getString("usuario")!!
            contrasenia = it.getString("contrasenia")!!
            ciudad = it.getLong("ciudad")
            calle = it.getString("calle")!!
            numero = it.getInt("numero")
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AccesoActivity)?.mostrarFragmentContainer()
    }

    override fun onPause() {
        super.onPause()
        (activity as? AccesoActivity)?.ocultarFragmentContainer()
    }

    private fun cargarEtiquetas() {
        selectedEtiquetasIds.clear()
        lifecycleScope.launch {
            val etiquetas = etiquetasDao.obtenerEtiquetas()
            var tableRow: TableRow? = null
            var checkBoxCount = 0

            for (etiqueta in etiquetas) {
                //Se crea una fila que contenga máximo 2 checkbox
                if (checkBoxCount % 2 == 0) {
                    tableRow = TableRow(requireContext())
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                }

                //Se crea el checkbox
                val checkBox = CheckBox(requireContext()).apply {
                    text = etiqueta.nombre
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    // Asociar el ID de la etiqueta con el CheckBox
                    tag = etiqueta.id_etiqueta // Guarda el ID en la propiedad tag
                }

                //Se agrega el checkbox a la row
                tableRow?.addView(checkBox)

                //Se aumenta el contador de checkbox en uno
                checkBoxCount++

                //Si el número de checkbox es par, se crea una roy nueva
                if (checkBoxCount % 2 == 0) {
                    tableLayout.addView(tableRow)
                }
            }
        }
    }

    private fun validacionIntereses(): Boolean {
        var checkedCount = 0

        // Recorremos cada fila en la tabla
        for (i in 0 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            // Recorremos cada CheckBox en la fila
            for (j in 0 until row.childCount) {
                val checkBox = row.getChildAt(j) as CheckBox
                if (checkBox.isChecked) {
                    checkedCount++ // Contamos los CheckBoxes marcados
                }
            }
        }

        return checkedCount >= 3 // Retornamos true si hay al menos 3 CheckBoxes marcados
    }

    private fun validacionRol(): Boolean{
        return checkbox_alumno.isChecked || checkbox_gestor.isChecked
    }

    private fun registrarse(){

        // Validar intereses antes de proceder
        if (!validacionIntereses()) {
            Toast.makeText(context, "Debes seleccionar al menos 3 intereses.", Toast.LENGTH_LONG).show()
            return
        }

        //Validar que se ha seleccionado un rol
        if(!validacionRol()){
            Toast.makeText(context, "Debes seleccionar al menos un objetivo.", Toast.LENGTH_LONG).show()
            return
        }

        //Recoge los checkbox marcados de intereses
        for (i in 0 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            for (j in 0 until row.childCount) {
                val checkBox = row.getChildAt(j) as CheckBox
                if (checkBox.isChecked) {
                    val idEtiqueta = checkBox.tag as Long // Obtener el ID guardado en tag
                    selectedEtiquetasIds.add(idEtiqueta) // Agregar ID a la lista
                }
            }
        }

        //Recoge los checkbox de Roles
        if(checkbox_alumno.isChecked)
            selectedRolesIds.add(3)
        if(checkbox_gestor.isChecked)
            selectedRolesIds.add(2)

        //Se llama a la función Dao para agregar el usuario
        val direccion = Direcciones(
            numero = numero,
            calle = calle,
            ciudad = ciudad,
            id_direccion = 0
        )

        val usuario = Usuarios(
            dni = dni,
            nombre = nombre,
            apellido = apellido,
            fechaNacimiento = nacimiento!!,
            email = correo,
            contraseña = PasswordUtils.hashPassword(contrasenia),
            direccion = direccion.id_direccion,
            activo = false,
            id_usuario = 0L,
            nombreUsuario = usuario
        )

        var isCreated = false

        viewLifecycleOwner.lifecycleScope.launch {
            isCreated = usuariosDao.crearUsuario(usuario, selectedRolesIds, selectedEtiquetasIds, direccion)
            //Ir de vuelta a AccesoActivity
            val intent = Intent(requireContext(), AccesoActivity::class.java)
            startActivity(intent)

            if(isCreated) {
                Toast.makeText(requireContext(), "Se ha registrado correctamente", Toast.LENGTH_SHORT)
                    .show()
            }
            else{
                Toast.makeText(requireContext(), "Ha ocurrido un error al registrar", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

}
