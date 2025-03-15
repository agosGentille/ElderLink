package tpi.tusi.data.daos

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Autoevaluacion
import tpi.tusi.ui.entities.Preguntas
import java.sql.DriverManager

class PreguntasDao {

    suspend fun guardarPreguntas(preguntas: List<Preguntas>, autoevaluacionId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false

                val query = """
                    INSERT INTO Preguntas (pregunta, respuestaCorrecta, respuestaIncorrecta1, respuestaIncorrecta2, respuestaIncorrecta3, estado, fk_autoevaluaciones) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """
                connection.prepareStatement(query).use { statement ->
                    for (pregunta in preguntas) {
                        statement.setString(1, pregunta.pregunta)
                        statement.setString(2, pregunta.respuestaCorrecta)
                        statement.setString(3, pregunta.respuestaIncorrecta1)
                        statement.setString(4, pregunta.respuestaIncorrecta2)
                        statement.setString(5, pregunta.respuestaIncorrecta3)
                        statement.setBoolean(6, true)
                        statement.setLong(7, autoevaluacionId)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }

                connection.commit()
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun obtenerPreguntasPorAutoevaluacionId(autoevaluacionId: Long): List<Preguntas> = withContext(Dispatchers.IO) {
        val preguntasList = mutableListOf<Preguntas>()

        val query = """
            SELECT *
            FROM Preguntas
            WHERE fk_autoevaluaciones = ? AND estado = true
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, autoevaluacionId)

                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        preguntasList.add(
                            Preguntas(
                                id_pregunta = resultSet.getLong("id_pregunta"),
                                pregunta = resultSet.getString("Pregunta"),
                                respuestaCorrecta = resultSet.getString("RespuestaCorrecta"),
                                respuestaIncorrecta1 = resultSet.getString("RespuestaIncorrecta1"),
                                respuestaIncorrecta2 = resultSet.getString("RespuestaIncorrecta2"),
                                respuestaIncorrecta3 = resultSet.getString("RespuestaIncorrecta3"),
                                estado = resultSet.getBoolean("estado"),
                                fk_autoevaluaciones = autoevaluacionId
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext preguntasList
    }

    suspend fun obtenerPreguntaConEnunciado(enunciado: String, autoevaluacionId: Long): Preguntas? = withContext(Dispatchers.IO) {
        var pregunta: Preguntas? = null
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                SELECT *
                FROM Preguntas 
                WHERE fk_autoevaluaciones = ? AND Pregunta = ? AND estado = true
            """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, autoevaluacionId)
                    statement.setString(2, enunciado)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        pregunta = Preguntas(
                            id_pregunta = resultSet.getLong("id_pregunta"),
                            pregunta = enunciado,
                            respuestaCorrecta = resultSet.getString("RespuestaCorrecta"),
                            respuestaIncorrecta1 = resultSet.getString("RespuestaIncorrecta1"),
                            respuestaIncorrecta2 = resultSet.getString("RespuestaIncorrecta2"),
                            respuestaIncorrecta3 = resultSet.getString("RespuestaIncorrecta3"),
                            estado = resultSet.getBoolean("estado"),
                            fk_autoevaluaciones = autoevaluacionId
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext pregunta
    }

    suspend fun deletePregunta(pregunta: Preguntas): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Preguntas SET 
                        estado = ? 
                    WHERE id_pregunta = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, false)
                    statement.setLong(2, pregunta.id_pregunta)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun updatePregunta(pregunta: Preguntas): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false

                val query = """
                    UPDATE Preguntas SET 
                        pregunta = ?, 
                        respuestaCorrecta = ?, 
                        respuestaIncorrecta1 = ?, 
                        respuestaIncorrecta2 = ?, 
                        respuestaIncorrecta3 = ?, 
                        estado = ? 
                    WHERE id_pregunta = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, pregunta.pregunta)
                    statement.setString(2, pregunta.respuestaCorrecta)
                    statement.setString(3, pregunta.respuestaIncorrecta1)
                    statement.setString(4, pregunta.respuestaIncorrecta2)
                    statement.setString(5, pregunta.respuestaIncorrecta3)
                    statement.setBoolean(6, pregunta.estado)
                    statement.setLong(7, pregunta.id_pregunta)
                    Log.d("DBUpdate", "Pregunta: $pregunta")
                    val affectedRows = statement.executeUpdate()
                    connection.commit()
                    Log.d("DBUpdate", "Filas afectadas: $affectedRows")
                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun deletePreguntasPorAutoevaluacion(autoevaluacion: Autoevaluacion): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Preguntas SET 
                        estado = false
                    WHERE fk_autoevaluaciones = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, autoevaluacion.id_autoevaluacion)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
