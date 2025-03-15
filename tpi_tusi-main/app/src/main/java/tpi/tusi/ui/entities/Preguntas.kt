package tpi.tusi.ui.entities

data class Preguntas(
    val id_pregunta: Long,
    val pregunta: String,
    val respuestaCorrecta: String,
    val respuestaIncorrecta1: String,
    val respuestaIncorrecta2: String,
    val respuestaIncorrecta3: String,
    var estado: Boolean,
    val fk_autoevaluaciones: Long
)
