package tpi.tusi.ui.entities

data class DenunciaDetalle (
    val denuncia_id: Long,
    var denuncia_razon: String,
    val fk_usuario_mail: String,
    val fk_curso_id: Long,
    val fk_curso_titulo: String
)