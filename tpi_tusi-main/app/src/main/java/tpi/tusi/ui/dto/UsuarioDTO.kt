package tpi.tusi.ui.dto

import java.sql.Date

data class UsuarioDTO(
    val id_usuario: Long = 0L,
    val dni: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nombreUsuario: String = "",
    val email: String = "",
    val fechaNacimiento: Date? = null,
    val activo: Boolean = false,
    val calle: String = "",
    val numero: Int = 0,
    val ciudad: String = "",
    val provincia: String = "",
    val pais: String = ""
)
