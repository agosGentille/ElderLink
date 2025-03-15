package tpi.tusi.ui.entities

import java.sql.Date

data class Eventos(
    val id_evento: Long? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val url_imagen: String? = null,
    val fecha: Date = Date(0),
    val activo: Boolean? = null,
)
