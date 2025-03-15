package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import java.sql.DriverManager

class RolesDao {

    suspend fun getRolesByUserById(id: Long): List<String?> = withContext(Dispatchers.IO){
        var roles = mutableListOf<String?>()
        var query = "" +
                "SELECT descripcion FROM `Roles` AS ro" +
                " INNER JOIN `RolesXUsuarios` AS ru" +
                " ON ro.id_rol = ru.fk_rol" +
                " INNER JOIN `Usuarios` AS u" +
                " ON u.id_usuario = ru.fk_usuario" +
                " WHERE u.id_usuario = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, id.toString().trim())
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        roles.add(rs.getString("descripcion"))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext roles
    }
}