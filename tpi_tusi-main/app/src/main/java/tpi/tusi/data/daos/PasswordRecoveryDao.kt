package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import java.sql.DriverManager
import java.sql.SQLException

class PasswordRecoveryDao {

    suspend fun deleteRowFromDB(
        id: Long) = withContext(Dispatchers.IO) {
        val query = "DELETE FROM PasswordRecovery WHERE fk_usuario = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserAndCode(
        id: Long,
        code: Int) = withContext(Dispatchers.IO) {
        val query = "INSERT INTO PasswordRecovery (" +
                " fk_usuario, lastCode) " +
                "VALUES (?, ?)"
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.setInt(2, code)
                    stmt.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }


    suspend fun getCodeByUserId(
        id_usuario: Long): Int = withContext(Dispatchers.IO) {
        var codigo: Int? = null
        val query = "SELECT pr.lastCode AS code FROM `PasswordRecovery` pr WHERE fk_usuario = ?"

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, id_usuario)
                    stmt.executeQuery().use { resultado ->
                        if (resultado.next()) {
                            codigo = resultado.getInt("code")
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        codigo!!
    }

}