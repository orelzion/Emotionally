package com.github.orelzion.emotionally.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ImageDetectionDetails(
    val faceRectangle: FaceRectangle? = null,
    val emotion: String? = null,
    val error: Error? = null
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
