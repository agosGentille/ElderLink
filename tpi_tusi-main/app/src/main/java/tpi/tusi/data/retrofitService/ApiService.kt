package tpi.tusi.data.retrofitService

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("v1/upload/file")
    suspend fun uploadImageTOAPI(
        @Part file: MultipartBody.Part
    ): ResponseBody
}