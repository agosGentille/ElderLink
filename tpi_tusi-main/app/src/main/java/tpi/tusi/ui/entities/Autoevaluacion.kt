package tpi.tusi.ui.entities

data class Autoevaluacion(
    val id_autoevaluacion: Long,
    val fk_curso: Long,
    val estado: Boolean
)
