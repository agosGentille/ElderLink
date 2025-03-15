package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Paises
import java.sql.DriverManager

class PaisesDao{

    suspend fun getPaisById(id: Long): Paises? = withContext(Dispatchers.IO) {
        var pais: Paises? = null

        val query = "select * from Paises where id_pais = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, id.toString())
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        pais = Paises(
                            id_pais = resultSet.getLong("id_pais"),
                            nombre = resultSet.getString("nombre")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext pais
    }

    suspend fun obtenerPaises(): List<Paises> = withContext(Dispatchers.IO) {
        val listaPaises = ArrayList<Paises>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("""
                        SELECT id_pais, nombre 
                        FROM tusi.Paises
                    """).use { resultSet ->
                        while (resultSet.next()) {
                            val pais = Paises(
                                id_pais = resultSet.getLong("id_pais"),
                                nombre = resultSet.getString("nombre")
                            )
                            listaPaises.add(pais)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaPaises
    }


}