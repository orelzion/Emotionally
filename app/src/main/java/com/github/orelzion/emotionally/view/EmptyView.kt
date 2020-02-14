package com.github.orelzion.emotionally.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.viewmodel.ImageDetectionState
import com.github.orelzion.emotionally.viewmodel.ImageDetectionViewModel

class EmptyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var viewModel: ImageDetectionViewModel? = null
    var stateLiveData: LiveData<ImageDetectionState>? = null
        set(value) {
            field = value
            value?.observe(context as LifecycleOwner, Observer<ImageDetectionState> {
                visibility = when (it) {
                    is ImageDetectionState.Empty -> {
                        View.VISIBLE
                    }
                    else -> View.GONE
                }
            })
        }

    init {
        View.inflate(context, R.layout.view_empty_state, this)
        setOnClickListener {
            viewModel?.onOpenImageChooserClicked()
        }
    }
}