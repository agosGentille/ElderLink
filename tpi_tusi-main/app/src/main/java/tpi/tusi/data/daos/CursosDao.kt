package tpi.tusi.data.daos

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Cursos
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class CursosDao {

    suspend fun getTitleCursoFromUserByIntereses(
        id: Long
    ): ArrayList<String> = withContext(Dispatchers.IO) {
        var intereses = ArrayList<String>()
        val query = "SELECT c.nombre FROM Cursos AS c " +
                "JOIN CursoXEtiqueta AS ce " +
                "ON c.id_curso = ce.fk_curso " +
                "JOIN Intereses AS i " +
                "ON ce.fk_etiqueta = i.fk_etiqueta " +
                "WHERE i.fk_usuario = ? " +
                "AND c.fecha_creacion BETWEEN CURDATE() " +
                "- INTERVAL 7 DAY AND CURDATE() " +
                "ORDER BY c.fecha_creacion DESC LIMIT 5"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setLong(1, id)
                    val resultSet = stmt.executeQuery()
                    while (resultSet.next()) {
                        val etiqueta = resultSet.getString("nombre")
                        intereses.add(etiqueta)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("Error en obtener intereses -> ", e.message.toString())
        }
        intereses
    }

    suspend fun obtenerCursos(
        idUsuario: Int? = null,
        nombre: String? = null,
        fkEtiqueta: Int? = null,
        fkEstadoCurso: Int? = null,
        usuario: Int? = null,
    ): List<Cursos> = withContext(Dispatchers.IO) {
        val listaCursos = ArrayList<Cursos>()

        // Construcción de la consulta completa con base y cláusula WHERE
        //val consultaCompleta = "${construirConsultaBase()} ${construirClausulaWhere(idUsuario, nombre, fkEtiqueta, fkEstadoCurso, usuario)}"
        //val consultaCompleta = "${construirConsultaBase(idUsuario)} ${construirClausulaWhere(idUsuario, nombre, fkEtiqueta, fkEstadoCurso, usuario)}"
        val consultaCompleta = """
            ${construirConsultaBase(idUsuario)}
            ${construirClausulaWhere(idUsuario, nombre, fkEtiqueta, fkEstadoCurso, usuario)}
            ORDER BY Cursos.fecha_actualizacion DESC
        """
        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(consultaCompleta).use { preparedStatement ->
                    var index = 1
                    idUsuario?.let {
                        preparedStatement.setInt(index++, it)
                        println("Parámetro idUsuario: $it")
                    }
                    nombre?.let {
                        preparedStatement.setString(index++, "%$it%")
                        println("Parámetro nombre: %$it%")
                    }
                    fkEtiqueta?.let {
                        preparedStatement.setInt(index++, it)
                        println("Parámetro fkEtiqueta: $it")
                    }
                    fkEstadoCurso?.let {
                        preparedStatement.setInt(index++, it)
                        println("Parámetro fkEstadoCurso: $it")
                    }
                    usuario?.let {
                        preparedStatement.setInt(index++, it)
                        println("Parámetro usuario (creador): $it")
                    }

                    preparedStatement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val curso = Cursos(
                                id_curso = resultSet.getLong("id_curso"),
                                nombre = resultSet.getString("nombre"),
                                descripcion = resultSet.getString("descripcion"),
                                fechaCreacion = resultSet.getTimestamp("fecha_creacion").toInstant(),
                                fechaActualizacion = resultSet.getTimestamp("fecha_actualizacion").toInstant(),
                                observacion = resultSet.getString("observacion"),
                                usuario = resultSet.getLong("usuario"),
                                estado_curso = resultSet.getLong("fk_estado_curso"),
                                thumbnailURL = resultSet.getString("thumbnailURL"),
                            )
                            listaCursos.add(curso)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listaCursos
    }

    private fun construirConsultaBase(idUsuario: Int?): String {
        val joinUsuariosXCurso = if (idUsuario != null) {
            """
        LEFT JOIN 
            tusi.UsuarioXCurso 
        ON 
            UsuarioXCurso.fk_curso = Cursos.id_curso
        """
        } else {
            ""
        }

        return """
        SELECT 
            Cursos.id_curso,
            Cursos.nombre,
            LEFT(Cursos.descripcion, 256) AS descripcion,
            Cursos.fecha_creacion,
            Cursos.fk_estado_curso,
            Cursos.fecha_actualizacion,
            LEFT(Cursos.observacion, 256) AS observacion,
            Cursos.fk_usuario AS usuario,
            Cursos.thumbnailURL
        FROM 
            tusi.Cursos 
        $joinUsuariosXCurso
    """
    }

    private fun construirClausulaWhere(
        idUsuario: Int? = null,
        nombre: String? = null,
        fkEtiqueta: Int? = null,
        fkEstadoCurso: Int? = null,
        usuario: Int? = null
    ): String {
        val clausulas = mutableListOf<String>()

        idUsuario?.let { clausulas.add("UsuarioXCurso.fk_usuario = ?") }
        nombre?.let { clausulas.add("Cursos.nombre LIKE ?") }
        fkEtiqueta?.let { clausulas.add("Cursos.id_curso IN (SELECT fk_curso FROM tusi.CursoXEtiqueta WHERE fk_etiqueta = ?)") }
        fkEstadoCurso?.let { clausulas.add("Cursos.fk_estado_curso = ?") }
        usuario?.let { clausulas.add("Cursos.fk_usuario = ?") }

        return if (clausulas.isNotEmpty()) "WHERE ${clausulas.joinToString(" AND ")}" else ""
    }

    private fun construirConsultaBase(): String = """
        SELECT 
            Cursos.id_curso,
            Cursos.nombre,
            LEFT(Cursos.descripcion, 256) AS descripcion,
            Cursos.fecha_creacion,
            Cursos.fk_estado_curso,
            Cursos.fecha_actualizacion,
            LEFT(Cursos.observacion, 256) AS observacion,
            Cursos.fk_usuario AS usuario,
            UsuarioXCurso.fecha_inicio,
            Cursos.thumbnailURL
        FROM 
            tusi.Cursos 
        LEFT JOIN 
            tusi.UsuarioXCurso 
        ON 
            UsuarioXCurso.fk_curso = Cursos.id_curso
    """

    suspend fun insertarCurso(curso: Cursos, etiquetasSeleccionadas: List<Long>): Boolean = withContext(Dispatchers.IO) {
        var exitoCurso = false
        var exitoEtiquetas = false

        try {
            // Insertar curso
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                val query = """
            INSERT INTO tusi.Cursos (
                nombre, descripcion, fecha_creacion, fecha_actualizacion,
                observacion, fk_usuario, fk_estado_curso, thumbnailURL
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """
                connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
                    statement.setString(1, curso.nombre)
                    statement.setString(2, curso.descripcion)
                    statement.setTimestamp(3, java.sql.Timestamp(curso.fechaCreacion.toEpochMilli()))
                    statement.setTimestamp(4, java.sql.Timestamp(curso.fechaActualizacion.toEpochMilli()))
                    statement.setString(5, curso.observacion)
                    statement.setLong(6, curso.usuario)
                    statement.setLong(7, curso.estado_curso)
                    statement.setString(8, curso.thumbnailURL)

                    // Ejecuta la inserción
                    val filasAfectadas = statement.executeUpdate()

                    // Obtener el ID del curso insertado
                    if (filasAfectadas > 0) {
                        val generatedKeys = statement.generatedKeys
                        if (generatedKeys.next()) {
                            val cursoId = generatedKeys.getLong(1) // Obtiene el ID generado
                            exitoCurso = true

                            // Ahora que tenemos el ID del curso, insertar las etiquetas
                            exitoEtiquetas = insertarEtiquetasCurso(cursoId, etiquetasSeleccionadas)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        // Devolver exito solo si ambos insertos son exitosos
        return@withContext exitoCurso && exitoEtiquetas
    }

    private suspend fun insertarEtiquetasCurso(cursoId: Long, etiquetasSeleccionadas: List<Long>): Boolean {
        var exito = false
        try {
            // Instanciar EtiquetasDao y llamar a insertarEtiquetasCurso
            val etiquetasDao = EtiquetasDao()
            exito = etiquetasDao.insertarEtiquetasCurso(cursoId, etiquetasSeleccionadas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return exito
    }

    suspend fun actualizarCurso(curso: Cursos, etiquetasSeleccionadas: List<Long>): Boolean = withContext(Dispatchers.IO) {
        var exitoCurso = false
        var exitoEtiquetas = false

        try {
            // Conexión a la base de datos
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->

                // Actualizar los datos del curso
                val query = """
                UPDATE tusi.Cursos SET 
                    nombre = ?, descripcion = ?, fecha_actualizacion = ?,
                    observacion = ?, fk_usuario = ?, fk_estado_curso = ?, thumbnailURL = ?
                WHERE id_curso = ?
            """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, curso.nombre)
                    statement.setString(2, curso.descripcion)
                    statement.setTimestamp(3, java.sql.Timestamp(curso.fechaActualizacion.toEpochMilli()))
                    statement.setString(4, curso.observacion)
                    statement.setLong(5, curso.usuario)
                    statement.setLong(6, curso.estado_curso)
                    statement.setString(7, curso.thumbnailURL)
                    statement.setLong(8, curso.id_curso)

                    // Ejecuta la actualización
                    exitoCurso = statement.executeUpdate() > 0
                }

                // Actualizar etiquetas asociadas al curso
                if (exitoCurso) {
                    val etiquetasDao = EtiquetasDao()
                    exitoEtiquetas = etiquetasDao.actualizarEtiquetasCurso(curso.id_curso, etiquetasSeleccionadas)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return@withContext exitoCurso && exitoEtiquetas
    }

    suspend fun obtenerCursoEspecifico(cursoId: Long): Cursos? = withContext(Dispatchers.IO) {
        var curso: Cursos? = null

        val query = """
                SELECT * FROM Cursos
                WHERE id_curso = ?
                """.trimIndent()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, cursoId)

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        curso = Cursos(
                            id_curso = resultSet.getLong("id_curso"),
                            nombre = resultSet.getString("nombre"),
                            descripcion = resultSet.getString("descripcion"),
                            fechaCreacion = resultSet.getTimestamp("fecha_creacion").toInstant(),
                            fechaActualizacion = resultSet.getTimestamp("fecha_actualizacion").toInstant(),
                            observacion = resultSet.getString("observacion"),
                            usuario = resultSet.getLong("fk_usuario"),
                            estado_curso = resultSet.getLong("fk_estado_curso"),
                            thumbnailURL = resultSet.getString("thumbnailURL")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext curso
    }

    suspend fun gestionarEstadoCurso(estado: Int, id_curso: Long): Boolean = withContext(Dispatchers.IO) {
        val query = """
        UPDATE Cursos 
        SET fk_estado_curso = ? 
        WHERE id_curso = ?
        """

        var isUpdated = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, estado)
                    statement.setLong(2, id_curso)

                    isUpdated = statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }

    suspend fun agregarCursoAUsuario(idUsuario: Long, idCurso: Long): Boolean = withContext(Dispatchers.IO) {
        val checkQuery = "SELECT COUNT(*) FROM UsuarioXCurso WHERE fk_usuario = ? AND fk_curso = ?"
        val insertQuery = """
        INSERT INTO UsuarioXCurso (fk_usuario, fk_curso, fecha_inicio)
        VALUES (?, ?, NULL)
    """

        var isInserted = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                // Primero, verifica si ya existe la relación
                connection.prepareStatement(checkQuery).use { checkStatement ->
                    checkStatement.setLong(1, idUsuario)
                    checkStatement.setLong(2, idCurso)

                    val resultSet = checkStatement.executeQuery()
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        // Ya existe la relación, lanzamos una excepción personalizada
                        throw IllegalArgumentException("El curso ya está agregado a 'Mis Cursos'.")
                    }
                }

                // Si no existe la relación, procedemos con la inserción
                connection.prepareStatement(insertQuery).use { insertStatement ->
                    insertStatement.setLong(1, idUsuario)
                    insertStatement.setLong(2, idCurso)

                    isInserted = insertStatement.executeUpdate() > 0
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e("CursosDao", e.message.toString()) // Mensaje descriptivo para curso existente
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isInserted
    }
    suspend fun actualizarFechaCursoUsuario(idUsuario: Long, idCurso: Long, fechaInicio: String): Boolean = withContext(Dispatchers.IO) {
        val query = """
        UPDATE UsuarioXCurso 
        SET fecha_inicio = ? 
        WHERE fk_usuario = ? AND fk_curso = ?
    """

        var isUpdated = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, fechaInicio)
                    statement.setLong(2, idUsuario)
                    statement.setLong(3, idCurso)

                    isUpdated = statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isUpdated
    }
    suspend fun eliminarCursoDeUsuario(idUsuario: Long, idCurso: Long): Boolean = withContext(Dispatchers.IO) {
        val query = """
        DELETE FROM UsuarioXCurso 
        WHERE fk_usuario = ? AND fk_curso = ?
    """

        var isDeleted = false

        try {
            Class.forName("com.mysql.jdbc.Driver")

            DriverManager.getConnection(DataDB.urlMySQL, DataDB.user, DataDB.pass).use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setLong(1, idUsuario)
                    statement.setLong(2, idCurso)

                    isDeleted = statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isDeleted
    }

}

