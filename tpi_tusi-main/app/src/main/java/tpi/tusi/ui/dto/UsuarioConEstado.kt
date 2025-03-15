package tpi.tusi.ui.dto

data class UsuarioConEstado(
    val id_usuario: Long = 0L,
    val nombre: String = "",
    val apellido: String = "",
    val nombreUsuario: String = "",
    val activo: Boolean = false,
    val estado: Int = 0
)