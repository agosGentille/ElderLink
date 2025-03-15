package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.math.roundToInt

class ReportesDao {

    suspend fun obtenerPromedioCalificacion(fechaInicio: String, fechaFinal: String): Int = withContext(
        Dispatchers.IO) {
        var promedio = 0

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                SELECT AVG(calificacion) AS promedio
                FROM tusi.NotasXUsuario
                WHERE fecha BETWEEN ? AND ?
            """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setString(1, fechaInicio)
                    preparedStatement.setString(2, fechaFinal)

                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            promedio = resultSet.getDouble("promedio").roundToInt()
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        promedio
    }

    suspend fun obtenerPorcentajeFinalizado(fechaInicio: String, fechaFinal: String): Int? = withContext(
        Dispatchers.IO) {
        var porcentajeFinalizado: Int? = null

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                WITH CursosIniciados AS (
                    SELECT fk_usuario, fk_curso
                    FROM UsuarioXEtapa s
                    JOIN Etapas e ON s.fk_etapa = e.id_etapa
                    WHERE s.fecha BETWEEN ? AND ?
                    GROUP BY fk_usuario, fk_curso
                ),
                CursosFinalizados AS (
                    SELECT s.fk_usuario, e.fk_curso
                    FROM UsuarioXEtapa s
                    JOIN Etapas e ON s.fk_etapa = e.id_etapa
                    WHERE s.estado = 1
                      AND s.fecha BETWEEN ? AND ?
                    GROUP BY s.fk_usuario, e.fk_curso
                    HAVING COUNT(DISTINCT e.id_etapa) = (
                        SELECT COUNT(*) 
                        FROM Etapas et 
                        WHERE et.fk_curso = e.fk_curso
                    )
                )
                SELECT 
                    COUNT(DISTINCT cf.fk_usuario) * 100.0 / NULLIF(COUNT(DISTINCT ci.fk_usuario), 0) AS porcentaje_finalizado
                FROM CursosIniciados ci
                LEFT JOIN CursosFinalizados cf 
                ON ci.fk_usuario = cf.fk_usuario AND ci.fk_curso = cf.fk_curso;
            """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    // Asignamos los valores de las fechas para los parámetros de la consulta
                    preparedStatement.setString(1, fechaInicio)
                    preparedStatement.setString(2, fechaFinal)
                    preparedStatement.setString(3, fechaInicio)
                    preparedStatement.setString(4, fechaFinal)

                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            porcentajeFinalizado = resultSet.getDouble("porcentaje_finalizado").roundToInt()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        porcentajeFinalizado
    }

    suspend fun obtenerPromedioValoraciones(fechaInicio: String, fechaFinal: String): Double? = withContext(Dispatchers.IO) {
        var promedioValoracion: Double? = null

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                SELECT AVG(valoracion) AS promedio_valoracion
                FROM Valoraciones
                WHERE fecha BETWEEN ? AND ?
            """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    // Asignamos los valores de las fechas para los parámetros de la consulta
                    preparedStatement.setString(1, fechaInicio)
                    preparedStatement.setString(2, fechaFinal)

                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            promedioValoracion = resultSet.getDouble("promedio_valoracion")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        promedioValoracion
    }

}