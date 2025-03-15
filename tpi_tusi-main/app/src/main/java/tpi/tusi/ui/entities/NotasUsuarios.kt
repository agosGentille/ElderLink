package tpi.tusi.ui.entities

import java.sql.Date

data class NotasUsuarios(
    val id_nota: Long,
    val fecha: Date,
    val calificacion: Int,
    val nro_intento: Int,
    val fk_autoevaluacion: Long,
    val fk_usuario: Long
)