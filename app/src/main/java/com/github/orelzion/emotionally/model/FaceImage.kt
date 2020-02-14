package com.github.orelzion.emotionally.model

import android.graphics.Bitmap
import com.github.orelzion.emotionally.model.network.Emotion

data class FaceImage(val croppedBitmap: Bitmap, val emotion: Emotion)