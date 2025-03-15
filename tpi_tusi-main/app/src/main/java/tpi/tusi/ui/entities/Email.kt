package tpi.tusi.ui.entities

data class Email(
    val recipient: String,
    val subject: String,
    val message: String
)