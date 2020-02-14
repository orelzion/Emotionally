package com.github.orelzion.emotionally.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.model.ChooseImageRequest
import com.github.orelzion.emotionally.viewmodel.ImageDetectionViewModel
import kotlinx.android.synthetic.main.fragment_image_chooser.*

class ChooseImageDialogFragment : DialogFragment() {

    var viewModel: ImageDetectionViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_chooser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryBtn.setOnClickListener {
            dismissAllowingStateLoss()
            viewModel?.onImageChooserClicked(ChooseImageRequest.OPEN_GALLERY)
        }
        cameraBtn.setOnClickListener {
            dismissAllowingStateLoss()
            viewModel?.onImageChooserClicked(ChooseImageRequest.OPEN_CAMERA)
        }
    }
}