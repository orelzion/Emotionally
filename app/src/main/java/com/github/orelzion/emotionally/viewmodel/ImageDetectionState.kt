package com.github.orelzion.emotionally.viewmodel

import android.graphics.Bitmap
import com.github.orelzion.emotionally.model.FaceImage
import java.io.Serializable

sealed class ImageDetectionState: Serializable {
    object Empty : ImageDetectionState()
    data class ImageSelected(val bitmap: Bitmap) : ImageDetectionState()
    data class FaceDetected(val faceImage: FaceImage) : ImageDetectionState()
    data class Error(val exception: Throwable) : ImageDetectionState()
}