package tpi.tusi.ui.entities

import java.time.Instant

data class UsuarioEtapa(
    val estado: Boolean,
    val fecha: Instant,
    val fk_etapa: Long,
    val fk_usuario: Long
)
