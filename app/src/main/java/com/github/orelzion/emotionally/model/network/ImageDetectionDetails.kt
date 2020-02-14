package com.github.orelzion.emotionally.model.network

import kotlinx.serialization.Serializable

@Serializable
data class ImageDetectionDetails(
    val faceRectangle: FaceRectangle?,
    val faceAttributes: FaceAttributes?
)

@Serializable
data class FaceRectangle(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int
)

@Serializable
data class FaceAttributes(private val emotion: HashMap<Emotion, Double>) {
    fun getEmotion(): Emotion? {
        return emotion.maxBy { it.value }?.key
    }
}

@Serializable
enum class Emotion {
    anger,
    contempt,
    disgust,
    fear,
    happiness,
    neutral,
    sadness,
    surprise
}

