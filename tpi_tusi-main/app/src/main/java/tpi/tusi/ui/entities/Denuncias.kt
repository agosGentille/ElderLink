package tpi.tusi.ui.entities

import java.time.Instant

data class Denuncias(
    val id_denuncia: Long,
    val fecha: Instant,
    val fk_curso: Long,
    val fk_usuario: Long,
    val razon: String
)
