package tpi.tusi.data.database

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://apitusi.coldmind.ar/api/"  // Cambia esto por tu URL base

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Nivel de logging, puedes usar NONE, BASIC, HEADERS o BODY
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Agrega el interceptor de logging
        .connectTimeout(30, TimeUnit.SECONDS) // Tiempo de conexión
        .readTimeout(30, TimeUnit.SECONDS)    // Tiempo de lectura
        .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo de escritura
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create()) // Conversor de Gson
        .build()

    // Método para crear una instancia del servicio Retrofit
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}
