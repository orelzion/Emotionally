package com.github.orelzion.emotionally.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.model.ActivityActionsListener
import com.github.orelzion.emotionally.model.ChooseImageRequest
import com.github.orelzion.emotionally.model.network.Repository
import com.github.orelzion.emotionally.model.network.faceDetectApi
import com.github.orelzion.emotionally.viewmodel.ImageDetectionState
import com.github.orelzion.emotionally.viewmodel.ImageDetectionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.EasyImage
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val permissionRequestCode = 543
    private val easyImage: EasyImage by lazy { EasyImage.Builder(this).build() }
    private lateinit var imageDetectionViewModel: ImageDetectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        imageDetectionViewModel = ViewModelProvider(
            this,
            ImageDetectionViewModel.BreedsListModelFactory(Repository(faceDetectApi))
        )[ImageDetectionViewModel::class.java].also {
            errorView.viewModel = it
            emptyView.viewModel = it
            selectedImageView.viewModel = it
        }

        with(imageDetectionViewModel.imageDetectionStateObserver) {
            emptyView.stateLiveData = this
            selectedImageView.stateLiveData = this
            errorView.stateLiveData = this
            observe(this@MainActivity, Observer { onStateChange(it) })
        }

        initActivityActionListener()
    }

    private fun onStateChange(state: ImageDetectionState?) {
        Timber.i("imageDetectionUiStateObserver is %s", state)
        progressView.visibility = when (state) {
            is ImageDetectionState.ImageSelected -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun initActivityActionListener() {
        imageDetectionViewModel.activityActionsListener = object : ActivityActionsListener {
            override fun requestPermissionCheck(requestedPermission: String) {
                Timber.i("permissionCheckObserver is called")
                requestPermission(requestedPermission)
            }

            override fun showImageSelectionDialog() {
                Timber.i("openImageChooserObserver was called")
                val imageChooserDialog = ChooseImageDialogFragment()
                imageChooserDialog.viewModel = imageDetectionViewModel
                imageChooserDialog.show(supportFragmentManager, "imageChooser")
            }

            override fun openImageSource(chooseImageRequest: ChooseImageRequest) {
                Timber.i(
                    "chooseImageRequestObserver was called, %s was requested",
                    chooseImageRequest
                )
                when (chooseImageRequest) {
                    ChooseImageRequest.OPEN_GALLERY -> easyImage.openGallery(this@MainActivity)
                    ChooseImageRequest.OPEN_CAMERA -> easyImage.openCameraForImage(this@MainActivity)
                }
            }
        }
    }

    private fun requestPermission(requestedPermission: String) {
        val permission = ContextCompat.checkSelfPermission(this, requestedPermission)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(requestedPermission),
                permissionRequestCode
            )
        } else {
            onPermissionResult(requestedPermission, true)
        }
    }

    private fun onPermissionResult(grantedPermission: String, granted: Boolean) {
        imageDetectionViewModel.onPermissionResult(grantedPermission, granted)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestCode -> {
                onPermissionResult(
                    permissions.first(),
                    !(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(
            requestCode,
            resultCode,
            data,
            this,
            imageDetectionViewModel.onImageSelected()
        )
    }
}
