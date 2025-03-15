package tpi.tusi.data.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Cursos
import java.sql.DriverManager

class CursoDAO {

    suspend fun obtenerCursoEspecifico(cursoId: Long): Cursos? = withContext(Dispatchers.IO) {
        var curso: Cursos? = null

        val query = """
                SELECT * FROM Cursos
                WHERE id_curso = ?
                """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, cursoId)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        curso = Cursos(
                            id_curso = resultSet.getLong("id_curso"),
                            nombre = resultSet.getString("nombre"),
                            descripcion = resultSet.getString("descripcion"),
                            fechaCreacion = resultSet.getTimestamp("fecha_creacion").toInstant(),
                            fechaActualizacion = resultSet.getTimestamp("fecha_actualizacion").toInstant(),
                            observacion = resultSet.getString("observacion"),
                            usuario = resultSet.getLong("fk_usuario"),
                            estado_curso = resultSet.getLong("fk_estado_curso"),
                            thumbnailURL = resultSet.getString("thumbnailURL")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext curso
    }

    suspend fun gestionarEstadoCurso(estado: Int, id_curso: Long): Boolean = withContext(Dispatchers.IO) {
        val query = """
        UPDATE Cursos 
        SET fk_estado_curso = ? 
        WHERE id_curso = ?
        """

        var isUpdated = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, estado)
                    statement.setLong(2, id_curso)

                    isUpdated = statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }

}
