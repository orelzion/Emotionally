package com.github.orelzion.emotionally.model.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class Repository(private val detectApi: FaceDetectApi) {
    suspend fun detectFace(imageFileToExem: File): ImageDetectionDetails {
        val requestBody = imageFileToExem
            .readBytes()
            .toRequestBody("application/octet-stream".toMediaType())

        return detectApi.detectFace(requestBody).first()
    }
}