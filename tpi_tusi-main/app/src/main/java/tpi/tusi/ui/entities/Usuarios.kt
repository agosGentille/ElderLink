package tpi.tusi.ui.entities

import java.sql.Date

data class Usuarios(
    val id_usuario: Long = 0L,
    val dni: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nombreUsuario: String = "",
    val email: String = "",
    val contraseña: String = "",
    val fechaNacimiento: Date = Date(0),
    val activo: Boolean = false,
    val direccion: Long = 0L
)