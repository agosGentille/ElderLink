package tpi.tusi.ui.dto

import java.sql.Date

data class UsuarioBasicoDTO(
    val id_usuario: Long = 0L,
    val dni: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nombreUsuario: String = "",
    val email: String = "",
    val activo: Boolean = false,
)
