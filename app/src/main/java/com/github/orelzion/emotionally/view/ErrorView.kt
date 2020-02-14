package com.github.orelzion.emotionally.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.model.error.FaceDetectionError
import com.github.orelzion.emotionally.model.error.InvalidImageError
import com.github.orelzion.emotionally.viewmodel.ImageDetectionState
import com.github.orelzion.emotionally.viewmodel.ImageDetectionViewModel
import kotlinx.android.synthetic.main.view_error.view.*
import timber.log.Timber

class ErrorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var viewModel: ImageDetectionViewModel? = null

    var stateLiveData: LiveData<ImageDetectionState>? = null
        set(value) {
            field = value
            value?.observe(context as LifecycleOwner, Observer<ImageDetectionState> {
                when (it) {
                    is ImageDetectionState.Error ->
                        onErrorState(it)
                    else -> visibility = View.GONE
                }
            })
        }

    init {
        View.inflate(context, R.layout.view_error, this)
    }

    private fun onErrorState(error: ImageDetectionState.Error) {

        Timber.e(error.exception, "Error state: ")

        visibility = View.VISIBLE

        errorMessage.text = when (error.exception) {
            is FaceDetectionError -> context.getString(R.string.face_detection_error)
            is InvalidImageError -> context.getString(R.string.invalid_image_error)
            else -> context.getString(R.string.general_error)
        }

        tryAgainBtn.setOnClickListener { viewModel?.onTryAgainClicked() }
        chooseAnotherBtn.setOnClickListener { viewModel?.onChooseAnotherClicked() }
    }
}