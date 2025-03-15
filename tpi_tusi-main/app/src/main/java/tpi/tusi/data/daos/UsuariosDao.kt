package tpi.tusi.data.daos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.dto.UsuarioBasicoDTO
import tpi.tusi.ui.dto.UsuarioConEstado
import tpi.tusi.ui.dto.UsuarioDTO
import tpi.tusi.ui.dto.UsuarioDireccionDTO
import tpi.tusi.ui.entities.Autoevaluacion
import tpi.tusi.ui.entities.Direcciones
import tpi.tusi.ui.entities.Usuarios
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class UsuariosDao {

    suspend fun updatePasswordUsuario(
        id: Long, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = "UPDATE Usuarios SET Contraseña = ? WHERE id_usuario = ? "
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, pass)
                    statement.setLong(2, id)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getIfUserExistByEmail(
        email: String): Boolean = withContext(Dispatchers.IO) {
            var exist = false
            val query = "SELECT EXISTS ( " +
                "SELECT 1 " +
                "FROM Usuarios " +
                "WHERE Email_Us = ? " +
                ") AS email_existe"
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { stmt ->
                    stmt.setString(1, email)
                    stmt.executeQuery().use { resultado ->
                        if (resultado.next()) {
                            exist = resultado.getBoolean("email_existe")
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        exist
    }

    suspend fun getUsuarioByEmail(email: String): Usuarios? = withContext(Dispatchers.IO) {
        var usuario: Usuarios? = null

        var query = "select * from Usuarios where Email_Us = ?"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, email)
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        usuario = Usuarios(
                            id_usuario = resultSet.getLong("id_usuario"),
                            dni = resultSet.getString("DNI_Us"),
                            nombre = resultSet.getString("Nombre_Us"),
                            apellido = resultSet.getString("Apellido_Us"),
                            nombreUsuario = resultSet.getString("NombreUsuario_Us"),
                            email = resultSet.getString("Email_Us"),
                            contraseña = resultSet.getString("Contraseña"),
                            fechaNacimiento = resultSet.getDate("fecha_nac"),
                            activo = resultSet.getBoolean("activo"),
                            direccion = resultSet.getLong("id_direc")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext usuario
    }
    suspend fun obtenerUsuarioById(idUsuario: Long): UsuarioBasicoDTO? = withContext(Dispatchers.IO) {
        var usuarioDTO: UsuarioBasicoDTO? = null
        val query = "SELECT id_usuario, DNI_Us, Nombre_Us, Apellido_Us, NombreUsuario_Us, Email_Us, activo FROM Usuarios WHERE id_usuario = ?"

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, idUsuario)
                    val resultSet = stmt.executeQuery()
                    if (resultSet.next()) {
                        usuarioDTO = UsuarioBasicoDTO(
                            id_usuario = resultSet.getLong("id_usuario"),
                            dni = resultSet.getString("DNI_Us"),
                            nombre = resultSet.getString("Nombre_Us"),
                            apellido = resultSet.getString("Apellido_Us"),
                            nombreUsuario = resultSet.getString("NombreUsuario_Us"),
                            email = resultSet.getString("Email_Us"),
                            activo = resultSet.getBoolean("activo")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext usuarioDTO
    }

    suspend fun obtenerGestoresFiltrados(filtroNombre: String, estado: Int = 0): List<UsuarioConEstado> = withContext(Dispatchers.IO) {
        val listaUsuarios = ArrayList<UsuarioConEstado>()
        var whereEstadoQuery = ""

        if(estado != 0){
            whereEstadoQuery = "AND ru.estado = $estado"
        }

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                SELECT id_usuario, Nombre_Us, Apellido_Us, NombreUsuario_Us, activo, ru.estado 
                FROM tusi.Usuarios u
                JOIN RolesXUsuarios ru ON u.id_usuario = ru.fk_usuario
                WHERE ru.fk_rol = 2 AND Nombre_Us LIKE ? $whereEstadoQuery
                ORDER BY ru.estado DESC
            """
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setString(1, "%$filtroNombre%")
                    preparedStatement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val usuario = UsuarioConEstado(
                                id_usuario = resultSet.getLong("id_usuario"),
                                nombre = resultSet.getString("Nombre_Us"),
                                apellido = resultSet.getString("Apellido_Us"),
                                nombreUsuario = resultSet.getString("NombreUsuario_Us"),
                                activo = resultSet.getBoolean("activo"),
                                estado = resultSet.getInt("ru.estado")
                            )
                            listaUsuarios.add(usuario)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaUsuarios
    }

    suspend fun obtenerGestores(): List<UsuarioConEstado> = withContext(Dispatchers.IO) {
        val listaUsuarios = ArrayList<UsuarioConEstado>()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("""
                        SELECT id_usuario, Nombre_Us, Apellido_Us, NombreUsuario_Us,
                        ru.estado, activo FROM tusi.Usuarios u
                        JOIN RolesXUsuarios ru ON u.id_usuario = ru.fk_usuario
                        WHERE ru.fk_rol = 2
                        ORDER BY ru.estado DESC
                    """).use { resultSet ->
                        while (resultSet.next()) {
                            val usuario = UsuarioConEstado(
                                id_usuario = resultSet.getLong("id_usuario"),
                                nombre = resultSet.getString("Nombre_Us"),
                                apellido = resultSet.getString("Apellido_Us"),
                                nombreUsuario = resultSet.getString("NombreUsuario_Us"),
                                activo = resultSet.getBoolean("activo"),
                                estado = resultSet.getInt("ru.estado")
                            )
                            listaUsuarios.add(usuario)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaUsuarios
    }

    suspend fun obtenerGestorConIDs(id: Long): UsuarioDireccionDTO = withContext(Dispatchers.IO) {
        var usuarioDto = UsuarioDireccionDTO(id = 0, dni = "", nombre = "", apellido = "", fechaNacimiento = "",
            nombreUsuario = "", pais = "", provincia = "", idProvincia = 0,
            ciudad = "", idCiudad = 0, calle = "", numero = 0)

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                SELECT 
                    u.id_usuario AS idUsuario, u.DNI_Us AS dni, u.Nombre_Us AS nombre, 
                    u.Apellido_Us AS apellido, u.NombreUsuario_Us AS nombreUsuario, 
                    u.Email_Us AS email, u.fecha_nac AS fechaNacimiento,
                    d.calle, d.numero,
                    c.id_ciudad AS idCiudad, c.nombre AS ciudad,
                    p.id_provincia AS idProvincia, p.nombre AS provincia,
                    pa.id_pais AS idPais, pa.nombre AS pais
                FROM tusi.Usuarios u
                INNER JOIN tusi.Direcciones d ON u.id_direc = d.id_direccion
                INNER JOIN tusi.Ciudades c ON c.id_ciudad = d.fk_ciudad
                INNER JOIN tusi.Provincias p ON p.id_provincia = c.fk_provincia
                INNER JOIN tusi.Paises pa ON pa.id_pais = p.fk_pais
                WHERE u.id_usuario = ?
            """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setLong(1, id)

                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            usuarioDto = UsuarioDireccionDTO(
                                id = resultSet.getLong("idUsuario"),
                                dni = resultSet.getString("dni"),
                                nombre = resultSet.getString("nombre"),
                                apellido = resultSet.getString("apellido"),
                                fechaNacimiento = resultSet.getString("fechaNacimiento"),
                                nombreUsuario = resultSet.getString("nombreUsuario"),
                                pais = resultSet.getString("pais"),
                                provincia = resultSet.getString("provincia"),
                                idProvincia = resultSet.getLong("idProvincia"),
                                ciudad = resultSet.getString("ciudad"),
                                idCiudad = resultSet.getLong("idCiudad"),
                                calle = resultSet.getString("calle"),
                                numero = resultSet.getInt("numero")
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        usuarioDto
    }


    suspend fun obtenerGestor(id:Long): UsuarioDTO = withContext(Dispatchers.IO) {
        var usuarioDto = UsuarioDTO()

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
                    SELECT u.*, d.calle, d.numero, c.nombre, p.nombre, pa.nombre FROM tusi.Usuarios u
                    INNER JOIN tusi.Direcciones d ON u.id_direc = d.id_direccion
                    INNER JOIN tusi.Ciudades c ON c.id_ciudad = d.fk_ciudad
                    INNER JOIN tusi.Provincias p ON p.id_provincia = c.fk_provincia
                    INNER JOIN tusi.Paises pa ON pa.id_pais = p.fk_pais
                    WHERE u.id_usuario = ?
                """.trimIndent()

                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setLong(1, id)

                    preparedStatement.executeQuery().use { resultSet ->
                        if(resultSet.next()) {
                            usuarioDto = UsuarioDTO(
                                id_usuario = resultSet.getLong("u.id_usuario"),
                                dni = resultSet.getString("u.DNI_Us"),
                                nombre = resultSet.getString("u.Nombre_Us"),
                                apellido = resultSet.getString("u.Apellido_Us"),
                                nombreUsuario = resultSet.getString("u.NombreUsuario_Us"),
                                email = resultSet.getString("u.Email_Us"),
                                fechaNacimiento = resultSet.getDate("u.fecha_nac"),
                                calle = resultSet.getString("d.calle"),
                                numero = resultSet.getInt("d.numero"),
                                ciudad = resultSet.getString("c.nombre"),
                                provincia = resultSet.getString("p.nombre"),
                                pais = resultSet.getString("pa.nombre")
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        usuarioDto
    }

    suspend fun gestionarEstadoGestor(estado: Int, id_usuario: Long): Boolean = withContext(Dispatchers.IO) {
        val query = """
        UPDATE RolesXUsuarios 
        SET estado = ? 
        WHERE fk_rol = 2 AND fk_usuario = ?
        """

        var isUpdated = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, estado)
                    statement.setLong(2, id_usuario)

                    isUpdated = statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }

    suspend fun crearUsuario(usuario: Usuarios, roles: List<Long>, intereses: List<Long>, direccion: Direcciones): Boolean = withContext(Dispatchers.IO) {
        var isCreated = false
        var direccionId: Long? = null
        var usuarioId: Long? = null

        val query = """
            INSERT INTO Direcciones (calle, numero, fk_ciudad) 
            VALUES (?, ?, ?)
        """

        //Intenta crear la dirección
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                // Cambiar a prepareStatement con el flag de generar claves
                connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, direccion.calle)
                    statement.setInt(2, direccion.numero)
                    statement.setLong(3, direccion.ciudad)

                    isCreated = statement.executeUpdate() > 0
                    if (isCreated) {
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            direccionId = generatedKeys.getLong(1) // Almacena el ID de la dirección
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val query1 = """
            INSERT INTO Usuarios (DNI_Us, Nombre_Us, Apellido_Us, NombreUsuario_Us, Email_Us, Contraseña, fecha_nac, id_direc, activo) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """

        //Intenta crear el usuario
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, usuario.dni)
                    statement.setString(2, usuario.nombre)
                    statement.setString(3, usuario.apellido)
                    statement.setString(4, usuario.nombreUsuario)
                    statement.setString(5, usuario.email)
                    statement.setString(6, usuario.contraseña)
                    statement.setDate(7, usuario.fechaNacimiento as Date?)
                    statement.setLong(8, direccionId!!)

                    statement.setInt(9, 1)

                    isCreated = statement.executeUpdate() > 0

                    if(isCreated){
                        // Obtiene las claves generadas
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            usuarioId = generatedKeys.getLong(1) // Almacena el ID de la dirección
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if(isCreated){
            //Si se creó correctamente, se añaden los roles en la tabla RolesXUsuarios
            for(rol in roles) {
                try {
                    Class.forName("com.mysql.jdbc.Driver")

                    val query2 = """
                    INSERT INTO RolesXUsuarios (fk_rol, fk_usuario, estado) 
                    VALUES (?, ?, ?)
                    """

                    DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                        .use { connection ->
                            connection.prepareStatement(query2).use { statement ->
                                statement.setLong(1, rol)
                                statement.setLong(2, usuarioId!!)
                                if(rol == 2L)
                                    statement.setInt(3, 3)
                                else
                                    statement.setInt(3, 1)

                                isCreated = statement.executeUpdate() > 0
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            //Se añaden las Etiquetas seleccionadas en la tabla Intereses
            try {
                Class.forName("com.mysql.jdbc.Driver")

                val query3 = """
                    INSERT INTO Intereses (fk_etiqueta, fk_usuario) 
                    VALUES (?, ?)
                """.trimIndent()

                DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                    connection.prepareStatement(query3).use { statement ->
                        // Recorremos la lista de intereses
                        for (interes in intereses) {
                            statement.setLong(1, interes)
                            statement.setLong(2, usuarioId!!)

                            statement.addBatch() // Añadimos la inserción al lote
                        }
                        val results = statement.executeBatch()
                        isCreated = results.all { it > 0 }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        isCreated

    }

    suspend fun validarMail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass)
                .use { connection ->
                    val query = """
                SELECT COUNT(*) FROM tusi.Usuarios WHERE Email_Us = ?
            """.trimIndent()

                    connection.prepareStatement(query).use { preparedStatement ->
                        preparedStatement.setString(1, email)

                        preparedStatement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                return@withContext resultSet.getInt(1) > 0
                            }
                        }
                    }
                }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        false
    }

    suspend fun deleteUsuario(idUsuario: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Usuarios SET 
                        activo = ? 
                    WHERE id_usuario = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, false)
                    statement.setLong(2, idUsuario)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun deleteRolUsuario(idUsuario: Long, idRol: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE RolesXUsuarios SET 
                        estado = ? 
                    WHERE fk_rol = ?, fk_usuario = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setBoolean(1, false)
                    statement.setLong(2, idUsuario)
                    statement.setLong(3, idRol)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun updateUsuario(usuario: Usuarios): Boolean = withContext(Dispatchers.IO) {
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.autoCommit = false
                val query = """
                    UPDATE Usuarios SET 
                        DNI_Us = ?,
                        Nombre_Us = ?,
                        Apellido_Us = ?,
                        NombreUsuario_Us = ?,
                        fecha_nac = ?,
                        id_direc = ?,
                        activo = ? 
                    WHERE id_usuario = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, usuario.dni)
                    statement.setString(2, usuario.nombre)
                    statement.setString(3, usuario.apellido)
                    statement.setString(4, usuario.nombreUsuario)
                    statement.setDate(5, usuario.fechaNacimiento)
                    statement.setLong(6, usuario.direccion)
                    statement.setBoolean(7, true)
                    statement.setLong(8, usuario.id_usuario)

                    val affectedRows = statement.executeUpdate()
                    connection.commit()

                    return@withContext affectedRows > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}