package tpi.tusi.ui.entities

import java.net.URL
import java.time.Instant

data class Cursos(
    val id_curso: Long,
    val nombre: String,
    val descripcion: String,
    val fechaCreacion: Instant,
    val fechaActualizacion: Instant,
    val observacion: String,
    val usuario: Long,
    val estado_curso: Long,
    val thumbnailURL: String
)

