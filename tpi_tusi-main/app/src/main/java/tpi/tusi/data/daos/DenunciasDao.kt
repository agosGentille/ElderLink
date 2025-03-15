package tpi.tusi.data.daos

import com.mysql.jdbc.Statement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Cursos
import tpi.tusi.ui.entities.DenunciaDetalle
import tpi.tusi.ui.entities.Denuncias
import tpi.tusi.ui.entities.Preguntas
import tpi.tusi.ui.entities.Usuarios
import java.sql.DriverManager
import java.sql.Date
import java.sql.SQLException

class DenunciasDao {

    suspend fun insertDenuncia(denuncia: Denuncias): Long? = withContext(Dispatchers.IO) {
        var denunciaId: Long? = null
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false

                val query = """
                    INSERT INTO Denuncias (fk_usuario, fk_curso, fecha, razon) 
                    VALUES (?, ?, ?, ?)
                """.trimIndent()
                val fecha = Date(denuncia.fecha.toEpochMilli())
                connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setLong(1, denuncia.fk_usuario)
                    statement.setLong(2, denuncia.fk_curso)
                    statement.setDate(3, fecha)
                    statement.setString(4, denuncia.razon)

                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected > 0) {
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            denunciaId = generatedKeys.getLong(1)
                        }
                    }
                }
                connection.commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext denunciaId
    }


    suspend fun obtenerDenunciasConDetalles(): List<DenunciaDetalle> = withContext(Dispatchers.IO) {
        val listaDenuncias = mutableListOf<DenunciaDetalle>()

        val query = """
        SELECT d.id_denuncia, d.razon, c.id_curso AS id_curso, c.nombre AS titulo_curso, u.Email_Us AS email_usuario
        FROM Denuncias d
        JOIN Cursos c ON d.fk_curso = c.id_curso
        JOIN Usuarios u ON d.fk_usuario = u.id_usuario
    """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val denunciaDetalle = DenunciaDetalle(
                                denuncia_id = resultSet.getLong("id_denuncia"),
                                denuncia_razon = resultSet.getString("razon"),
                                fk_curso_id = resultSet.getLong("id_curso"),
                                fk_curso_titulo = resultSet.getString("titulo_curso"),
                                fk_usuario_mail = resultSet.getString("email_usuario")
                            )
                            listaDenuncias.add(denunciaDetalle)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaDenuncias
    }

    suspend fun deleteDenuncia(idDenuncia: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    DELETE FROM Denuncias 
                    WHERE id_denuncia = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, idDenuncia)

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