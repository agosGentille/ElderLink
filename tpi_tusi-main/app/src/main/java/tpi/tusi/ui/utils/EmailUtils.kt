package tpi.tusi.ui.utils

import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Email
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailUtils(
    private val username: String,
    private val password: String
) {
    private fun getMailSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", DataDB.smtpHost)
            put("mail.smtp.socketFactory.port", DataDB.smtpPort)
            put("mail.smtp.socketFactory.class", DataDB.smtpClass)
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", DataDB.smtpPort)
        }
        return Session.getInstance(props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
    }

    fun sendEmail(email: Email): Boolean {
        val session = getMailSession()
        var exito = false
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                addRecipient(Message.RecipientType.TO, InternetAddress(email.recipient))
                subject = email.subject
                setText(email.message)
            }
            Transport.send(message)
            println("Correo enviado correctamente")
            exito = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return exito
    }

}