package tpi.tusi.ui.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class DateUtils {
    fun calcularEdad(cumple: LocalDate): Int {
        val fechaActual = LocalDate.now()
        return Period.between(cumple, fechaActual).years
    }
    fun calculateAgeFromString(birthDateString: String, pattern: String = "yyyy-MM-dd"): Int {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val birthDate = LocalDate.parse(birthDateString, formatter)
        return calcularEdad(birthDate)
    }
}