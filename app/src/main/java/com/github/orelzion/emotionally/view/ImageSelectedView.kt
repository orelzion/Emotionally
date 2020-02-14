package com.github.orelzion.emotionally.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.model.FaceImage
import com.github.orelzion.emotionally.viewmodel.ImageDetectionState
import kotlinx.android.synthetic.main.view_image_selected.view.*

class ImageSelectedView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var stateLiveData: LiveData<ImageDetectionState>? = null
        set(value) {
            field = value
            value?.observe(context as LifecycleOwner, Observer<ImageDetectionState> {
                when (it) {
                    is ImageDetectionState.ImageSelected -> {
                        onImageSelectedState(it.bitmap)
                    }
                    is ImageDetectionState.FaceDetected -> {
                        onSuccessState(it.faceImage)
                    }
                    else -> visibility = View.GONE
                }
            })
        }

    init {
        View.inflate(context, R.layout.view_image_selected, this)
    }

    private fun onImageSelectedState(bitmap: Bitmap) {
        selectedImage.setImageBitmap(bitmap)
        visibility = View.VISIBLE
        emotionText.text = null
    }

    private fun onSuccessState(faceImage: FaceImage) {
        visibility = View.VISIBLE
        emotionText.text = faceImage.emotion.toString()
        selectedImage.setImageBitmap(faceImage.croppedBitmap)
    }
}