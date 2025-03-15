package tpi.tusi.ui.entities

import java.time.Instant

data class UsuarioCurso(
    val fecha_inicio: Instant,
    val fk_curso: Long,
    val fk_usuario: Long
)
