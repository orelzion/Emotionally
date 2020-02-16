package com.github.orelzion.emotionally.viewmodel

import android.Manifest
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.github.orelzion.emotionally.createTempFile
import com.github.orelzion.emotionally.getBitmapInSize
import com.github.orelzion.emotionally.model.ActivityActionsListener
import com.github.orelzion.emotionally.model.ChooseImageRequest
import com.github.orelzion.emotionally.model.FaceImage
import com.github.orelzion.emotionally.model.ImageFile
import com.github.orelzion.emotionally.model.error.FaceDetectionError
import com.github.orelzion.emotionally.model.error.InvalidImageError
import com.github.orelzion.emotionally.model.error.NoSelectedImageError
import com.github.orelzion.emotionally.model.network.FaceDetectionDetails
import com.github.orelzion.emotionally.model.network.FaceDetectionResponse
import com.github.orelzion.emotionally.model.network.FaceRectangle
import com.github.orelzion.emotionally.model.network.Repository
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import timber.log.Timber
import java.io.File

class ImageDetectionViewModel(private val repository: Repository) : ViewModel() {

    private val cameraPermission = Manifest.permission.CAMERA

    class ImageDetectionViewModelFactory(private val repository: Repository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ImageDetectionViewModel(repository) as T
        }
    }

    private val _stateLiveData: MutableLiveData<ImageDetectionState> = MutableLiveData()
    val imageDetectionStateObserver: LiveData<ImageDetectionState> = _stateLiveData
    var activityActionsListener: ActivityActionsListener? = null

    private var selectedImageFile: ImageFile? = null

    init {
        Timber.i("ImageDetectionViewModel init")
        _stateLiveData.postValue(ImageDetectionState.Empty)
    }

    fun onImageSelected(): EasyImage.Callbacks {
        return object : EasyImage.Callbacks {
            override fun onCanceled(source: MediaSource) {

            }

            override fun onImagePickerError(error: Throwable, source: MediaSource) {
                _stateLiveData.value = ImageDetectionState.Error(error)
            }

            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                val file = imageFiles.first().file
                file.getBitmapInSize(1920, 1080)?.let {
                    val newFile = it.createTempFile()
                    selectedImageFile = ImageFile(newFile, it)
                    detectFace(newFile, it)
                } ?: run {
                    reportError(InvalidImageError())
                }
            }
        }

    }

    private fun detectFace(file: File, bitmap: Bitmap) {
        _stateLiveData.value = ImageDetectionState.ImageSelected(bitmap)

        viewModelScope.launch {
            try {
                onReceiveDetectionResponse(repository.detectFace(file))
            } catch (exception: Exception) {
                reportError(FaceDetectionError(error = exception))
            }
        }
    }

    private fun onReceiveDetectionResponse(faceDetectionResponse: FaceDetectionResponse) {
        selectedImageFile?.run {
            faceDetectionResponse.faceDetectionDetails?.run {
                reportDetected(this, bitmap)
            } ?: run {
                reportError(FaceDetectionError(faceDetectionResponse.error?.errorMessage))
            }
        } ?: run {
            reportError(NoSelectedImageError("Tried to move to Success state, but the previous state was not ImageSelected"))
        }
    }

    private fun reportDetected(detectionDetails: FaceDetectionDetails, previousBitmap: Bitmap) {
        val (faceRectangle, emotion) = detectionDetails
        cropImageToFace(previousBitmap, faceRectangle)?.let {
            FaceImage(it, emotion)
        }?.also {
            _stateLiveData.postValue(ImageDetectionState.FaceDetected(it))
        }
    }

    private fun reportError(error: Throwable) {
        _stateLiveData.postValue(ImageDetectionState.Error(error))
    }

    private fun cropImageToFace(bitmap: Bitmap, faceRectangle: FaceRectangle): Bitmap? {
        return Bitmap.createBitmap(
            bitmap,
            faceRectangle.left,
            faceRectangle.top,
            faceRectangle.width,
            faceRectangle.height
        )
    }

    fun onTryAgainClicked() {
        selectedImageFile?.run {
            detectFace(file, bitmap)
        } ?: run {
            reportError(NoSelectedImageError("Tried to move to ImageSelected state, but no image file is presented"))
        }
    }

    fun onChooseAnotherClicked() {
        _stateLiveData.postValue(ImageDetectionState.Empty)
        onOpenImageChooserClicked()
    }

    fun onPermissionResult(permission: String, granted: Boolean) {
        if (cameraPermission == permission) {
            when {
                granted -> openCamera()
            }
        }
    }

    private fun openCamera() {
        activityActionsListener?.openImageSource(ChooseImageRequest.OPEN_CAMERA)
    }

    private fun openGallery() {
        activityActionsListener?.openImageSource(ChooseImageRequest.OPEN_GALLERY)
    }

    fun onImageChooserClicked(chooseImageRequest: ChooseImageRequest) {
        when (chooseImageRequest) {
            ChooseImageRequest.OPEN_GALLERY -> openGallery()
            ChooseImageRequest.OPEN_CAMERA -> requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        activityActionsListener?.requestPermissionCheck(cameraPermission)
    }

    fun onOpenImageChooserClicked() {
        activityActionsListener?.showImageSelectionDialog()
    }

    override fun onCleared() {
        super.onCleared()
        activityActionsListener = null
    }
}