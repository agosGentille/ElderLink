package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Ciudades
import tpi.tusi.ui.entities.Usuarios
import java.sql.DriverManager
import java.sql.SQLException

class CiudadesDao {

    suspend fun getCityById(c: Long): Ciudades? = withContext(Dispatchers.IO) {
        var ciudad: Ciudades? = null

        var query = "select * from Ciudades where id_ciudad = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, c.toString())
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        ciudad = Ciudades(
                            id_ciudad = resultSet.getLong("id_ciudad"),
                            nombre = resultSet.getString("nombre"),
                            provincia = resultSet.getLong("fk_provincia")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext ciudad
    }

    suspend fun obtenerCiudadesProvincia(idProvincia: Long): List<Ciudades> = withContext(Dispatchers.IO) {
        val listaCiudades = ArrayList<Ciudades>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                    SELECT *
                    FROM tusi.Ciudades
                    WHERE fk_provincia = ?
                """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setLong(1, idProvincia)

                    preparedStatement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val ciudad = Ciudades(
                                id_ciudad = resultSet.getLong("id_ciudad"),
                                nombre = resultSet.getString("nombre"),
                                provincia = resultSet.getLong("fk_provincia")
                            )
                            listaCiudades.add(ciudad)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listaCiudades
    }
}
