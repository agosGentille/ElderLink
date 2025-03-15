package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Direcciones
import tpi.tusi.ui.entities.Usuarios
import java.sql.DriverManager
import java.sql.Statement

class DireccionDao {
    suspend fun getDireccionById(id: Long): Direcciones? = withContext(Dispatchers.IO) {
        var direccion: Direcciones? = null

        var query = "select * from Direcciones where id_direccion = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, id.toString())
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        direccion = Direcciones(
                            id_direccion = resultSet.getLong("id_direccion"),
                            calle = resultSet.getString("calle"),
                            numero = resultSet.getInt("numero"),
                            ciudad = resultSet.getLong("fk_ciudad")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext direccion
    }

    suspend fun getDirecPorCalleNumYCiud(direccion: Direcciones): Direcciones? = withContext(Dispatchers.IO) {
        var direc: Direcciones? = null

        var query = "SELECT * FROM Direcciones " +
                "WHERE calle = ? AND numero = ? AND fk_ciudad = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, direccion.calle)
                    stmt.setInt(2, direccion.numero)
                    stmt.setLong(3, direccion.ciudad)

                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        direc = Direcciones(
                            id_direccion = resultSet.getLong("id_direccion"),
                            calle = resultSet.getString("calle"),
                            numero = resultSet.getInt("numero"),
                            ciudad = resultSet.getLong("fk_ciudad")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext direc
    }

    suspend fun insertDireccion(direccion: Direcciones): Long? = withContext(Dispatchers.IO) {
        var direccionId: Long? = null
        var isCreated = false
        val query = """
            INSERT INTO Direcciones (calle, numero, fk_ciudad) 
            VALUES (?, ?, ?)
        """
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
                        .use { statement ->
                            statement.setString(1, direccion.calle)
                            statement.setInt(2, direccion.numero)
                            statement.setLong(3, direccion.ciudad)

                            isCreated = statement.executeUpdate() > 0
                            if (isCreated) {
                                val generatedKeys = statement.generatedKeys
                                if (generatedKeys.next()) {
                                    direccionId = generatedKeys.getLong(1)
                                }
                            }
                        }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext direccionId
    }
}