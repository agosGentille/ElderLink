package tpi.tusi.ui.config

import java.security.MessageDigest
import java.util.Base64

object PasswordUtils {
    fun hashPassword(pass: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashdB = digest.digest(pass.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashdB)
    }
    fun verifyPassword(passIn: String, passDb: String): Boolean {
        val hashedInput = hashPassword(passIn)
        return hashedInput == passDb
    }
    fun validandoPassword(pass: String): Boolean {
        val regex = "^(?=.*\\d)(?=.*[A-Z]).{8,}$".toRegex()
        return regex.matches(pass)
    }
}