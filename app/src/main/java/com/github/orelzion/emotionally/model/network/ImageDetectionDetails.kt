package com.github.orelzion.emotionally.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ImageDetectionDetails(
    val faceRectangle: FaceRectangle,
    val faceAttributes: FaceAttributes
)

@Serializable
data class FaceRectangle(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int
)

@Serializable
data class FaceAttributes(val emotion: Emotion)

@Serializable
data class Emotion(
    val anger: Double,
    val contempt: Double,
    val disgust: Double,
    val fear: Double,
    val happiness: Double,
    val neutral: Double,
    val sadness: Double,
    val surprise: Double
) {
    fun getBestRated() =
        listOf(anger, contempt, disgust, fear, happiness, neutral, sadness, surprise).max()
}

