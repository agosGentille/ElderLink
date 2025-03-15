package tpi.tusi.ui.entities

import java.time.Instant

data class Valoraciones(
    val valoracion: Int,
    val fecha: Instant,
    val fk_usuario: Long,
    val fk_curso: Long
)
