package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import java.sql.DriverManager
import java.sql.SQLException

class RolesUsuarioDao {

    suspend fun getEstadoByRolGestorByUser(
        id_usuario: Long): String = withContext(Dispatchers.IO) {
            var estado: String = null.toString()
            val query = "SELECT ec.nombre " +
                    "from RolesXUsuarios ru " +
                    "INNER JOIN EstadoCurso as ec " +
                    "ON ru.estado = ec.id_estado_curso " +
                    "WHERE fk_usuario = ? " +
                    "AND fk_rol = 2"

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, id_usuario)
                    stmt.executeQuery().use { resultado ->
                        if (resultado.next()) {
                            estado = resultado.getString("nombre")
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        estado
    }
}