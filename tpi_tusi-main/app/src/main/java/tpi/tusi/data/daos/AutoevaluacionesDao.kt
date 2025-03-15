package tpi.tusi.data.daos

import com.mysql.jdbc.Statement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Autoevaluacion
import tpi.tusi.ui.entities.Etapas
import tpi.tusi.ui.entities.NotasUsuarios
import java.sql.DriverManager

class AutoevaluacionesDao {

    suspend fun crearAutoevaluacion(autoevaluacion: Autoevaluacion): Long? = withContext(Dispatchers.IO) {
        var autoevaluacionId: Long? = null

        val query = """
        INSERT INTO Autoevaluaciones (fk_curso, estado) 
        VALUES (?, ?)
    """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setLong(1, autoevaluacion.fk_curso)
                    statement.setBoolean(2, true)

                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected > 0) {
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            autoevaluacionId = generatedKeys.getLong(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext autoevaluacionId
    }

    suspend fun obtenerAutoevaluacionPorCursoId(cursoId: Long): Autoevaluacion? = withContext(Dispatchers.IO) {
        var autoevaluacion: Autoevaluacion? = null

        val query = """
            SELECT *
            FROM Autoevaluaciones 
            WHERE fk_curso = ? AND estado = true
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, cursoId)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        autoevaluacion = Autoevaluacion(
                            id_autoevaluacion = resultSet.getLong("id_autoevaluacion"),
                            fk_curso = cursoId,
                            estado = resultSet.getBoolean("estado")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext autoevaluacion
    }

    suspend fun deleteAutoevaluacion(autoeval: Autoevaluacion): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Autoevaluaciones SET 
                        estado = ? 
                    WHERE id_autoevaluacion = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, false)
                    statement.setLong(2, autoeval.id_autoevaluacion)

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

    suspend fun darDeAltaAutoevaluacion(autoeval: Autoevaluacion): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Autoevaluaciones SET 
                        estado = ? 
                    WHERE id_autoevaluacion = ? AND fk_curso = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, true)
                    statement.setLong(2, autoeval.id_autoevaluacion)
                    statement.setLong(3, autoeval.fk_curso)

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

    suspend fun cargarNotaAutoevaluacion(notasUsuario: NotasUsuarios): Boolean = withContext(Dispatchers.IO) {
        var isCreated = false
        var nroIntento = 1

        val selectQuery = """
        SELECT COUNT(*) FROM NotasXUsuario WHERE fk_usuario = ? AND fk_autoevalulacion = ?
    """.trimIndent()

        val insertQuery = """
        INSERT INTO NotasXUsuario (fk_autoevalulacion, fk_usuario, fecha, Calificacion, Nro_Intento) 
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(selectQuery).use { selectStatement ->
                    selectStatement.setLong(1, notasUsuario.fk_usuario)
                    selectStatement.setLong(2, notasUsuario.fk_autoevaluacion)

                    val resultSet = selectStatement.executeQuery()
                    if (resultSet.next()) {
                        nroIntento = resultSet.getInt(1) + 1
                    }
                }

                connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS).use { insertStatement ->
                    insertStatement.setLong(1, notasUsuario.fk_autoevaluacion)
                    insertStatement.setLong(2, notasUsuario.fk_usuario)
                    insertStatement.setDate(3, notasUsuario.fecha)
                    insertStatement.setInt(4, notasUsuario.calificacion)
                    insertStatement.setInt(5, nroIntento)

                    isCreated = insertStatement.executeUpdate() > 0

                    isCreated
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isCreated
    }

    suspend fun obtenerIntentos(autoeval: Long, usuario: Long): List<NotasUsuarios> = withContext(Dispatchers.IO) {

        val listaIntentos = ArrayList<NotasUsuarios>()

        val query = """
            SELECT * 
            FROM NotasXUsuario 
            WHERE fk_autoevalulacion = ? and fk_usuario = ?
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, autoeval)
                    statement.setLong(2, usuario)

                    val resultSet = statement.executeQuery()
                    while(resultSet.next()) {
                        val notasUsuario = NotasUsuarios(
                            calificacion = resultSet.getInt("Calificacion"),
                            fk_usuario = usuario,
                            id_nota = resultSet.getLong("id_nota"),
                            nro_intento = resultSet.getInt("Nro_intento"),
                            fk_autoevaluacion = autoeval,
                            fecha = resultSet.getDate("fecha")
                        )
                        listaIntentos.add(notasUsuario)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaIntentos

    }


}