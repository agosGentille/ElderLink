package tpi.tusi.data.database


object DataDB {
    // Configuración de la base de datos MySQL
    const val urlMySQL = "jdbc:mysql://coldmind.ar:3306/tusi"
    const val user = "utn"
    const val pass = "utntusi"

    const val adminEmail = "lamofa@gmail.com"
    const val usernameGmail = "coldmindsoluciones@gmail.com"
    const val passwordGmail = "yivf hchn cibm oixk"
    const val smtpHost = "smtp.gmail.com"
    const val smtpPort = "465"
    const val smtpClass = "javax.net.ssl.SSLSocketFactory"
}



    //@JvmStatic
    //fun getConnection(): Connection? {
    //  var connection: Connection? = null
    //  try {
    //      // Usa la clase del driver para la versión 5.1.x
    ////      Class.forName("com.mysql.jdbc.Driver")
    //    connection = DriverManager.getConnection(URL, USER, PASSWORD)
    //  } catch (e: ClassNotFoundException) {
    //      e.printStackTrace()
    //  } catch (e: SQLException) {
    //      e.printStackTrace()
    //  }
    //  return connection
    //}

