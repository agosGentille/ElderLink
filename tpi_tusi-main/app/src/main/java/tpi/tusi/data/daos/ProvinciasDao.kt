package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Provincias
import java.sql.DriverManager
import java.sql.SQLException

class ProvinciasDao {

    suspend fun getProvinciaById(id: Long): Provincias? = withContext(Dispatchers.IO) {
        var provincia: Provincias? = null

        val query = "select * from Provincias where id_provincia = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, id.toString())
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        provincia = Provincias(
                            id_provincia = resultSet.getLong("id_provincia"),
                            nombre = resultSet.getString("nombre"),
                            pais = resultSet.getLong("fk_pais")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext provincia
    }

    suspend fun obtenerProvinciasPais(idPais: Long): List<Provincias> = withContext(Dispatchers.IO) {
        val listaProvincias = ArrayList<Provincias>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                    SELECT *
                    FROM tusi.Provincias
                    WHERE fk_pais = ?
                """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setLong(1, idPais)

                    preparedStatement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val provincia = Provincias(
                                id_provincia = resultSet.getLong("id_provincia"),
                                nombre = resultSet.getString("nombre"),
                                pais = resultSet.getLong("fk_pais")
                            )
                            listaProvincias.add(provincia)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaProvincias
    }
}