package com.github.orelzion.emotionally.model.network

import com.github.orelzion.emotionally.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FaceDetectApi {
    @POST("face/v1.0/detect?returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=emotion&recognitionModel=recognition_01&returnRecognitionModel=false&detectionModel=detection_01")
    @Headers("Content-Type: application/octet-stream")
    suspend fun detectFace(@Body imageBody: RequestBody): List<ImageDetectionDetails>
}


// Logging requests
private val level =
    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
private val loggingInterceptor = HttpLoggingInterceptor().setLevel(level)


// Creating http client
private val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.FACE_API_KEY)
            .build()
        chain.proceed(request)
    }
    .addInterceptor(loggingInterceptor)
    .build()


// Creating API instance
val faceDetectApi = Retrofit.Builder()
    .baseUrl("https://francecentral.api.cognitive.microsoft.com/")
    .addConverterFactory(Json.nonstrict.asConverterFactory("application/json".toMediaType()))
    .client(client)
    .build()
    .create(FaceDetectApi::class.java)