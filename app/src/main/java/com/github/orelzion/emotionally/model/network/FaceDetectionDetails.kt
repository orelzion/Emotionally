package com.github.orelzion.emotionally.model.network

import kotlinx.serialization.Serializable

@Serializable
data class FaceDetectionResponse(
    val faceDetectionDetails: FaceDetectionDetails? = null,
    val error: Error? = null
)

@Serializable
data class FaceDetectionDetails(
    val faceRectangle: FaceRectangle,
    val emotion: String
)

@Serializable
data class FaceRectangle(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int
)

@Serializable
data class Error(val errorMessage: String)
