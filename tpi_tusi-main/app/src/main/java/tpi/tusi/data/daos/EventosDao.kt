package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Eventos
import java.sql.DriverManager
import java.sql.Statement

class EventosDao {

    suspend fun crearEventos(evento: Eventos, etiquetas: List<Long>): Boolean = withContext(
        Dispatchers.IO) {
        var isCreated = false
        var eventoId: Long? = null

        val query = """
            INSERT INTO Eventos (titulo, descripcion, url_imagen, fecha, activo) 
            VALUES (?, ?, ?, ?, ?)
        """

        //Intenta crear el evento
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    //Cambiar a prepareStatement con el flag de generar claves
                    connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
                        .use { statement ->
                            statement.setString(1, evento.titulo)
                            statement.setString(2, evento.descripcion)
                            statement.setString(3, evento.url_imagen)
                            statement.setDate(4, evento.fecha)
                            statement.setBoolean(5, evento.activo!!)

                            isCreated = statement.executeUpdate() > 0
                            if (isCreated) {
                                val generatedKeys = statement.generatedKeys
                                if (generatedKeys.next()) {
                                    eventoId =
                                        generatedKeys.getLong(1) // Almacena el ID del evento
                                }
                            }
                        }
                }

            val query2 = """
                    INSERT INTO EventoXEtiqueta (fk_evento, fk_etiqueta) 
                    VALUES (?, ?)
                """.trimIndent()

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query2).use { statement ->
                    // Recorremos la lista de etiquetas
                    for (etiqueta in etiquetas) {
                        statement.setLong(1, eventoId!!)
                        statement.setLong(2, etiqueta)

                        statement.addBatch() // Añadimos la inserción al lote
                    }
                    // Ejecutamos todas las inserciones en una sola llamada a la base de datos
                    val results = statement.executeBatch() // Ejecuta el batch
                    isCreated = results.all { it > 0 } // Verificamos si todas las inserciones fueron exitosas
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        isCreated
    }

    suspend fun traerEvento(evento: Long): Eventos? = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement("""
                SELECT * 
                FROM tusi.Eventos 
                WHERE id_evento = ?
            """.trimIndent()).use { preparedStatement ->

                    preparedStatement.setLong(1, evento)
                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            return@withContext Eventos(
                                id_evento = resultSet.getLong("id_evento"),
                                titulo = resultSet.getString("titulo"),
                                descripcion = resultSet.getString("descripcion"),
                                url_imagen = resultSet.getString("url_imagen"),
                                fecha = resultSet.getDate("fecha"),
                                activo = resultSet.getBoolean("activo")
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun traerEtiquetasEvento(evento: Long): List<Long> = withContext(Dispatchers.IO) {
        val listaEtiquetas = ArrayList<Long>()
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement("""
                SELECT * 
                FROM tusi.EventoXEtiqueta 
                WHERE fk_evento = ?
            """.trimIndent()).use { preparedStatement ->

                    preparedStatement.setLong(1, evento)
                    preparedStatement.executeQuery().use { resultSet ->
                        while(resultSet.next()) {
                            listaEtiquetas.add(resultSet.getLong("fk_etiqueta"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        listaEtiquetas
    }

    suspend fun modificarEvento(idEvento:Long, evento: Eventos, etiquetas: List<Long>): Boolean = withContext(Dispatchers.IO) {
        var isUpdated = false

        // Consulta para actualizar el evento
        val query = """
        UPDATE Eventos 
        SET titulo = ?, descripcion = ?, url_imagen = ?, fecha = ?, activo = ? 
        WHERE id_evento = ?
        """.trimIndent()

        // Intenta actualizar el evento
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, evento.titulo)
                        statement.setString(2, evento.descripcion)
                        statement.setString(3, evento.url_imagen)
                        statement.setDate(4, evento.fecha)
                        statement.setBoolean(5, evento.activo!!)
                        statement.setLong(6, idEvento)

                        isUpdated =
                            statement.executeUpdate() > 0 // Comprueba si se actualizó alguna fila
                    }
                }

            // Consulta para actualizar las etiquetas del evento
            val query2 = """
            DELETE FROM EventoXEtiqueta 
            WHERE fk_evento = ?
        """.trimIndent()

            // Primero, eliminamos las etiquetas actuales
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query2).use { statement ->
                        statement.setLong(1, idEvento)
                        statement.executeUpdate() // Ejecuta la eliminación
                    }
                }

            // Luego, insertamos las nuevas etiquetas
            val query3 = """
            INSERT INTO EventoXEtiqueta (fk_evento, fk_etiqueta) 
            VALUES (?, ?)
        """.trimIndent()

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query3).use { statement ->
                        // Recorremos la lista de etiquetas
                        for (etiqueta in etiquetas) {
                            statement.setLong(1, idEvento)
                            statement.setLong(2, etiqueta)
                            statement.addBatch() // Añadimos la inserción al lote
                        }
                        // Ejecutamos todas las inserciones en una sola llamada a la base de datos
                        statement.executeBatch() // Ejecuta el batch
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }

    suspend fun eliminarEvento(idEvento:Long): Boolean = withContext(Dispatchers.IO) {
        var isUpdated = false

        val query = """
        UPDATE Eventos 
        SET activo = 0 
        WHERE id_evento = ?
        """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query).use { statement ->
                        statement.setLong(1, idEvento)

                        isUpdated = statement.executeUpdate() > 0
                    }
                }

            // Consulta para actualizar las etiquetas del evento
            val query2 = """
            DELETE FROM EventoXEtiqueta 
            WHERE fk_evento = ?
        """.trimIndent()

            // Eliminamos las etiquetas actuales
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query2).use { statement ->
                        statement.setLong(1, idEvento)
                        statement.executeUpdate()
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }

    suspend fun obtenerEventos(): List<Eventos> = withContext(Dispatchers.IO) {
        val listaEventos = ArrayList<Eventos>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("""
                        SELECT * 
                        FROM tusi.Eventos 
                        WHERE activo = 1
                    """).use { resultSet ->
                        while (resultSet.next()) {
                            val evento = Eventos(
                                id_evento = resultSet.getLong("id_evento"),
                                titulo = resultSet.getString("titulo"),
                                descripcion = resultSet.getString("descripcion"),
                                url_imagen = resultSet.getString("url_imagen"),
                                fecha = resultSet.getDate("fecha"),
                                activo = resultSet.getBoolean("activo")
                            )
                            listaEventos.add(evento)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaEventos
    }

    suspend fun obtenerEventosTitulo(titulo: String): List<Eventos> = withContext(Dispatchers.IO) {
        val listaEventos = ArrayList<Eventos>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                SELECT * 
                FROM tusi.Eventos 
                WHERE activo = 1
                AND titulo LIKE ?
            """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, "%$titulo%")

                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val evento = Eventos(
                                id_evento = resultSet.getLong("id_evento"),
                                titulo = resultSet.getString("titulo"),
                                descripcion = resultSet.getString("descripcion"),
                                url_imagen = resultSet.getString("url_imagen"),
                                fecha = resultSet.getDate("fecha"),
                                activo = resultSet.getBoolean("activo")
                            )
                            listaEventos.add(evento)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaEventos
    }

}