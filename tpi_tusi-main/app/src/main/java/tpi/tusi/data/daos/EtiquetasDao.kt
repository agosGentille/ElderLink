package tpi.tusi.data.daos

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Etiquetas
import java.sql.DriverManager
import java.sql.SQLException

class EtiquetasDao {

    suspend fun obtenerEtiquetas(): List<Etiquetas> = withContext(Dispatchers.IO) {
        val listaEtiquetas = ArrayList<Etiquetas>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = "SELECT * FROM tusi.Etiquetas"

                connection.createStatement().use { statement ->
                    statement.executeQuery(query).use { resultSet ->
                        while (resultSet.next()) {
                            val etiqueta = Etiquetas(
                                id_etiqueta = resultSet.getLong("id_etiqueta"),
                                nombre = resultSet.getString("nombre")
                            )
                            listaEtiquetas.add(etiqueta)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaEtiquetas
    }

    suspend fun obtenerEtiquetasEvento(id: Long): List<Etiquetas> = withContext(Dispatchers.IO) {
        val listaEtiquetas = ArrayList<Etiquetas>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->

                val query = """SELECT * FROM tusi.Etiquetas e 
                |INNER JOIN tusi.EventoXEtiqueta ee ON e.id_etiqueta = ee.fk_etiqueta 
                |WHERE ee.fk_evento = ?""".trimMargin()

                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, id)

                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val etiqueta = Etiquetas(
                                id_etiqueta = resultSet.getLong("id_etiqueta"),
                                nombre = resultSet.getString("nombre")
                            )
                            listaEtiquetas.add(etiqueta)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaEtiquetas
    }


    suspend fun insertarEtiquetasCurso(cursoId: Long, etiquetas: List<Long>): Boolean = withContext(Dispatchers.IO) {
        var exito = true

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                INSERT INTO tusi.CursoXEtiqueta (fk_curso, fk_etiqueta) VALUES (?, ?)
            """

                connection.prepareStatement(query).use { statement ->
                    connection.autoCommit = false // Usamos transacciones para asegurar que todas las inserciones sean exitosas

                    for (etiquetaId in etiquetas) {
                        Log.d("CrearCursoFragment", "Preparando inserción de etiqueta con ID: $etiquetaId")
                        statement.setLong(1, cursoId)
                        statement.setLong(2, etiquetaId)
                        statement.addBatch() // Preparamos la inserción en batch
                    }

                    val resultado = statement.executeBatch()
                    connection.commit() // Confirmamos todas las inserciones
                    exito = resultado.all { it == 1 } // Si todas las inserciones fueron exitosas
                    Log.d("CrearCursoFragment", "Resultado de inserción de etiquetas: $resultado")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            exito = false
            Log.e("CrearCursoFragment", "Error al insertar etiquetas: ${e.message}")
        }

        return@withContext exito
    }
    suspend fun obtenerEtiquetasPorCurso(idCurso: Long): List<Etiquetas> = withContext(Dispatchers.IO) {
        val listaEtiquetasCurso = ArrayList<Etiquetas>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = "SELECT e.id_etiqueta, e.nombre FROM tusi.Etiquetas e " +
                        "JOIN tusi.CursoXEtiqueta ce ON e.id_etiqueta = ce.fk_etiqueta " +
                        "WHERE ce.fk_curso = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, idCurso)

                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val etiqueta = Etiquetas(
                                id_etiqueta = resultSet.getLong("id_etiqueta"),
                                nombre = resultSet.getString("nombre")
                            )
                            listaEtiquetasCurso.add(etiqueta)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaEtiquetasCurso
    }

    suspend fun actualizarEtiquetasCurso(cursoId: Long, etiquetas: List<Long>): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->

                // Eliminar etiquetas existentes
                val deleteQuery = "DELETE FROM tusi.CursoXEtiqueta WHERE fk_curso = ?"
                connection.prepareStatement(deleteQuery).use { deleteStatement ->
                    deleteStatement.setLong(1, cursoId)
                    deleteStatement.executeUpdate()
                }

                // Insertar nuevas etiquetas
                val insertQuery = "INSERT INTO tusi.CursoXEtiqueta (fk_curso, fk_etiqueta) VALUES (?, ?)"
                connection.prepareStatement(insertQuery).use { insertStatement ->
                    etiquetas.forEach { etiquetaId ->
                        insertStatement.setLong(1, cursoId)
                        insertStatement.setLong(2, etiquetaId)
                        insertStatement.addBatch()
                    }
                    insertStatement.executeBatch()
                }
            }
            true
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

}
