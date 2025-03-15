package tpi.tusi.ui.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import tpi.tusi.data.database.RetrofitClient
import tpi.tusi.data.retrofitService.ApiService
import java.io.File
import java.io.FileOutputStream

object ImageUploadUtil {
    // Función suspend para subir imagen a la API
    suspend fun uploadImageToApi(context: Context, uri: Uri): String? {
        return try {
            // Crea un archivo temporal
            val file = File.createTempFile("desdelaapp_", ".png", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Crea el cuerpo de la solicitud con la imagen
            val requestBody = file.asRequestBody("image/png".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)

            // Llamada a Retrofit
            val apiService = RetrofitClient.create(ApiService::class.java)
            val responseBody = apiService.uploadImageTOAPI(filePart)

            // Procesa la respuesta
            val jsonResponse = responseBody.string()
            val path = JSONObject(jsonResponse).getString("path")
            Log.d("ImageUploadUtil", "URL de la imagen recibida: $path")
            path

        } catch (e: Exception) {
            Log.e("ImageUploadUtil", "Error al subir la imagen: ${e.message}")
            null
        }
    }
}