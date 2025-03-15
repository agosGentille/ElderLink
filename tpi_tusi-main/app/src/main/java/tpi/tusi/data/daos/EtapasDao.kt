package tpi.tusi.data.daos

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Autoevaluacion
import tpi.tusi.ui.entities.Etapas
import tpi.tusi.ui.entities.Preguntas
import tpi.tusi.ui.entities.UsuarioEtapa
import tpi.tusi.ui.entities.Valoraciones
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Instant

class EtapasDao {

    suspend fun insertEtapa(etapa: Etapas): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    INSERT INTO Etapas (titulo, contenido, activo, fk_curso) 
                    VALUES (?, ?, ?, ?)
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, etapa.titulo)
                    statement.setString(2, etapa.contenido)
                    statement.setBoolean(3, true)
                    statement.setLong(4, etapa.fk_curso)
                    statement.executeUpdate()
                }
                connection.commit()
                return@withContext true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun updateEtapa(etapa: Etapas): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Etapas 
                    SET 
                        titulo = ?, 
                        contenido = ?, 
                        activo = ?, 
                        fk_curso = ? 
                    WHERE id_etapa = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, etapa.titulo)
                    statement.setString(2, etapa.contenido)
                    statement.setBoolean(3, true)
                    statement.setLong(4, etapa.fk_curso)
                    statement.setLong(5, etapa.id_etapa)
                    statement.executeUpdate()
                }
                connection.commit()
                return@withContext true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun deleteEtapa(idEtapa: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Etapas 
                    SET
                        activo = false
                    WHERE id_etapa = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, idEtapa)
                    statement.executeUpdate()
                }
                connection.commit()
                return@withContext true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getEtapaById(idEtapa: Long): Etapas? {
        return withContext(Dispatchers.IO) {
            var e: Etapas? = null
            try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                    connection.autoCommit = false
                    val query = """
                    SELECT * FROM Etapas 
                    WHERE id_etapa = ?
                """.trimIndent()
                    connection.prepareStatement(query).use { statement ->
                        statement.setLong(1, idEtapa)

                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            e = Etapas(
                                id_etapa = idEtapa,
                                titulo = resultSet.getString("titulo"),
                                activo = resultSet.getBoolean("activo"),
                                contenido = resultSet.getString("contenido"),
                                fk_curso = resultSet.getLong("fk_curso")
                            )
                        }
                    }
                }
            } catch (e: SQLException) {
                Log.e("DatabaseError", "Error while fetching Etapa by user and id", e)
            }
            e
        }
    }

    suspend fun getEtapasPorCurso(cursoId: Long): List<Etapas> = withContext(Dispatchers.IO){
        var listaEtapas = mutableListOf<Etapas>()

        val query = """
            SELECT *
            FROM Etapas
            WHERE fk_curso = ? AND activo = true
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, cursoId)

                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        listaEtapas.add(
                            Etapas(
                                id_etapa = resultSet.getLong("id_etapa"),
                                titulo = resultSet.getString("titulo"),
                                contenido = resultSet.getString("contenido"),
                                activo = resultSet.getBoolean("activo"),
                                fk_curso = resultSet.getLong("fk_curso")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext listaEtapas
    }

    suspend fun insertEtapasXUsuario(etapa: UsuarioEtapa): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    INSERT INTO UsuarioXEtapa (fk_usuario, fk_etapa, fecha, estado) 
                    VALUES (?, ?, ?, ?)
                """.trimIndent()
                val fecha = Date(etapa.fecha.toEpochMilli())
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, etapa.fk_usuario)
                    statement.setLong(2, etapa.fk_etapa)
                    statement.setDate(3, fecha)
                    statement.setBoolean(4, true)
                    statement.executeUpdate()
                }
                connection.commit()
                return@withContext true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun updateEstadoYFechaEtapaLeida(idEtapa: Long, idUsuario: Long, estado: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE UsuarioXEtapa 
                    SET estado = ?, fecha = ?
                    WHERE fk_usuario = ? AND fk_etapa = ?
                """.trimIndent()
                val fecha = Date(Instant.now().toEpochMilli())
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, estado)
                    statement.setDate(2, fecha)
                    statement.setLong(3, idUsuario)
                    statement.setLong(4, idEtapa)
                    statement.executeUpdate()
                }
                connection.commit()
                return@withContext true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getEtapaByUserAndId(idEtapa: Long, idUsuario: Long): UsuarioEtapa? {
        return withContext(Dispatchers.IO) {
            var etapaLeida: UsuarioEtapa? = null
            try {
                Class.forName("com.mysql.jdbc.Driver")
                DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                    connection.autoCommit = false
                    val query = """
                    SELECT * FROM UsuarioXEtapa 
                    WHERE fk_etapa = ? AND fk_usuario = ?
                """.trimIndent()
                    connection.prepareStatement(query).use { statement ->
                        statement.setLong(1, idEtapa)
                        statement.setLong(2, idUsuario)

                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            val fecha = Instant.now()
                            etapaLeida = UsuarioEtapa(
                                fk_etapa = idEtapa,
                                fk_usuario = idUsuario,
                                estado = resultSet.getBoolean("estado"),
                                fecha = resultSet.getDate("fecha").toInstant()
                            )
                        }
                    }
                }
            } catch (e: SQLException) {
                Log.e("DatabaseError", "Error while fetching Etapa by user and id", e)
            }
            etapaLeida
        }
    }

    suspend fun validarEtapasLeidas(cursoId: Long, usuarioId: Long): List<Etapas> = withContext(Dispatchers.IO) {
        var etapasLeidas = mutableListOf<Etapas>()

        val query = """
            SELECT e.id_etapa, e.titulo, e.contenido, e.activo, e.fk_curso
            FROM Etapas e
            JOIN UsuarioXEtapa uxe ON e.id_etapa = uxe.fk_etapa
            WHERE e.fk_curso = ?
                AND uxe.fk_usuario = ?
                AND e.activo = true
                AND uxe.estado = true
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, cursoId)
                    statement.setLong(2, usuarioId)

                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        var etapa = Etapas(
                            id_etapa = resultSet.getLong("id_etapa"),
                            titulo = resultSet.getString("titulo"),
                            contenido = resultSet.getString("contenido"),
                            activo = resultSet.getBoolean("activo"),
                            fk_curso = resultSet.getLong("fk_curso")
                        )
                        etapasLeidas.add(etapa)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext etapasLeidas
    }
}