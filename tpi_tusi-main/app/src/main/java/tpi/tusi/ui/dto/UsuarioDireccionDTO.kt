package tpi.tusi.ui.dto

import java.sql.Date

data class UsuarioDireccionDTO(
    val id: Long,
    val dni: String,
    val nombre: String,
    val apellido: String,
    val fechaNacimiento: String,
    val nombreUsuario: String,
    val pais: String,
    val provincia: String,
    val idProvincia: Long,
    val ciudad: String,
    val idCiudad: Long,
    val calle: String,
    val numero: Int
)
