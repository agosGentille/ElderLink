package tpi.tusi.data.daos

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Valoraciones
import java.sql.DriverManager
import java.sql.Date

class ValoracionesDao {

    suspend fun insertValoracion(valoracion: Valoraciones): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val checkQuery = """
                    SELECT COUNT(*) FROM Valoraciones 
                    WHERE fk_usuario = ? AND fk_curso = ?
                """.trimIndent()

                val exists = connection.prepareStatement(checkQuery).use { statement ->
                    statement.setLong(1, valoracion.fk_usuario)
                    statement.setLong(2, valoracion.fk_curso)
                    val resultSet = statement.executeQuery()
                    resultSet.next() && resultSet.getInt(1) > 0
                }

                if (exists) {
                    // Actualizar la valoración existente
                    val updateQuery = """
                        UPDATE Valoraciones 
                        SET valoracion = ?, fecha = ? 
                        WHERE fk_usuario = ? AND fk_curso = ?
                    """.trimIndent()
                    connection.prepareStatement(updateQuery).use { statement ->
                        statement.setInt(1, valoracion.valoracion)
                        statement.setDate(2, Date(valoracion.fecha.toEpochMilli()))
                        statement.setLong(3, valoracion.fk_usuario)
                        statement.setLong(4, valoracion.fk_curso)
                        statement.executeUpdate()
                    }
                    Log.d("valoracion", "Valoración actualizada para el usuario ${valoracion.fk_usuario} en el curso ${valoracion.fk_curso}")
                } else {
                    // Insertar una nueva valoración
                    val insertQuery = """
                        INSERT INTO Valoraciones (fk_usuario, fk_curso, valoracion, fecha) 
                        VALUES (?, ?, ?, ?)
                    """.trimIndent()
                    connection.prepareStatement(insertQuery).use { statement ->
                        statement.setLong(1, valoracion.fk_usuario)
                        statement.setLong(2, valoracion.fk_curso)
                        statement.setInt(3, valoracion.valoracion)
                        statement.setDate(4, Date(valoracion.fecha.toEpochMilli()))
                        statement.executeUpdate()
                    }
                    Log.d("valoracion", "Nueva valoración insertada para el usuario ${valoracion.fk_usuario} en el curso ${valoracion.fk_curso}")
                }

                connection.commit()
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}