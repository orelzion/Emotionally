package com.github.orelzion.emotionally

import android.Manifest
import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.orelzion.emotionally.model.ActivityActionsListener
import com.github.orelzion.emotionally.model.ChooseImageRequest
import com.github.orelzion.emotionally.model.network.FaceDetectionDetails
import com.github.orelzion.emotionally.model.network.FaceDetectionResponse
import com.github.orelzion.emotionally.model.network.Repository
import com.github.orelzion.emotionally.viewmodel.ImageDetectionState
import com.github.orelzion.emotionally.viewmodel.ImageDetectionViewModel
import com.jraska.livedata.test
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.aprilapps.easyphotopicker.MediaFile
import java.io.File

class ImageDetectionViewModelTest {
    private lateinit var viewModel: ImageDetectionViewModel
    private lateinit var repository: Repository
    private lateinit var activityListener: ActivityActionsListener

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initViewModel() {
        Dispatchers.setMain(mainThreadSurrogate)
        repository = mockk()
        viewModel = ImageDetectionViewModel(repository)
        activityListener = mockk<ActivityActionsListener>(relaxed = true)
        viewModel.activityActionsListener = activityListener
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun `Test that initial state is empty`() {
        //Given: view model was just created

        //When: first call to state observer is performed

        //Then: state is Empty
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.Empty)
    }

    @Test
    fun `Test user taps the empty view`() {
        //Given: state is Empty

        //When: user taps the empty view
        viewModel.onOpenImageChooserClicked()

        //Then: `showImageSelectionDialog` should be called
        verify { activityListener.showImageSelectionDialog() }
    }

    @Test
    fun `Test user taps the gallery icon`() {
        //Given: state is empty, user see image selection dialog

        //When: user choose gallery
        viewModel.onImageChooserClicked(ChooseImageRequest.OPEN_GALLERY)

        //Then: `openImageSource` with OPEN_GALLERY as parameter should be called
        verify { activityListener.openImageSource(ChooseImageRequest.OPEN_GALLERY) }
    }

    @Test
    fun `Test user taps the camera icon`() {
        //Given: state is empty, user see image selection dialog

        //When: user choose gallery
        viewModel.onImageChooserClicked(ChooseImageRequest.OPEN_CAMERA)

        //Then: request permission should be called
        verify { activityListener.requestPermissionCheck(Manifest.permission.CAMERA) }
    }

    @Test
    fun `Test if permission were not given, view model is not opening camera`() {
        //Given: state is empty, app requested permission to access camera

        //When: permission was not given
        viewModel.onPermissionResult(Manifest.permission.CAMERA, false)

        //Then: State is still empty, and view model does not open the camera
        verify(exactly = 0) { activityListener.openImageSource(ChooseImageRequest.OPEN_CAMERA) }
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.Empty)
    }

    @Test
    fun `Test if permission were given, view model call to open the camera`() {
        //Given: state is empty, app requested to access camera

        //When: camera permission was granted
        viewModel.onPermissionResult(Manifest.permission.CAMERA, true)

        //Then: view model to open the camera
        verify { activityListener.openImageSource(ChooseImageRequest.OPEN_CAMERA) }
    }

    @Test
    fun `Test image was selected, state is updated to ImageSelected`() {
        //Given: state is empty, user chose an image
        val bitmap = mockk<Bitmap>(relaxed = true)
        val mediaFile = mockMediaFile(bitmap)

        //When: user choose an image
        viewModel.onImageSelected().onMediaFilesPicked(arrayOf(mediaFile), mockk())

        //Then: State changes to ImageSelected and API call to detect API is made
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.ImageSelected(bitmap))

        coVerify { repository.detectFace(any()) }
    }

    @Test
    fun `Test image selection resulted an error, state is updated to Error`() {
        //Given: state is empty, user chose an image, image selection resulted an error
        val exception = mockk<Throwable>()

        //When: image selection resulted an error
        viewModel.onImageSelected().onImagePickerError(exception, mockk())

        //Then: State is changed to Error, API call was not made
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.Error(exception))

        coVerify(exactly = 0) { repository.detectFace(any()) }
    }

    @Test
    fun `Test image was selected, API throws an error`() {
        //Given: state is empty, user chose an image. API call resulted an error
        val bitmap = mockk<Bitmap>(relaxed = true)
        val mediaFile = mockMediaFile(bitmap)
        val exception = Exception("")
        coEvery { repository.detectFace(any()) } throws exception

        //When: user choose an image
        viewModel.onImageSelected().onMediaFilesPicked(arrayOf(mediaFile), mockk())

        //Then: State is changed to Error, API call was made
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue { it.javaClass == ImageDetectionState.Error::class.java }

        coVerify { repository.detectFace(any()) }
    }

    @Test
    fun `Test image was selected, API returns valid answer`() {
        //Given: state is empty, user chose an image. API returns valid answer
        val bitmap = mockk<Bitmap>(relaxed = true)
        val mediaFile = mockMediaFile(bitmap)
        val faceDetectionDetails = FaceDetectionDetails(mockk(relaxed = true), "happy")
        val response = FaceDetectionResponse(faceDetectionDetails = faceDetectionDetails)
        coEvery { repository.detectFace(any()) } returns response

        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(bitmap, any(), any(), any(), any()) } returns bitmap

        //When: user choose an image
        viewModel.onImageSelected().onMediaFilesPicked(arrayOf(mediaFile), mockk())

        //Then: State is changed FaceDetected
        viewModel.imageDetectionStateObserver
            .test()
            .awaitNextValue()
            .assertValue {
                ((it as ImageDetectionState.FaceDetected).faceImage).run {
                    this.croppedBitmap == bitmap && this.emotion == faceDetectionDetails.emotion
                }
            }
    }

    private fun mockMediaFile(bitmap: Bitmap): MediaFile {
        val file = mockk<File>()
        mockkStatic("com.github.orelzion.emotionally.ImageUtilsKt")
        every { file.getBitmapInSize(any(), any()) } returns bitmap
        every { bitmap.createTempFile() } returns mockk()
        return MediaFile(mockk(), file)
    }

    @Test
    fun `Test user click choose another`() {
        //Given: we are in error state
        val exception = mockk<Throwable>()
        viewModel.onImageSelected().onImagePickerError(exception, mockk())

        //When: user taps choose another button
        viewModel.onChooseAnotherClicked()

        //Then: state is back in empty, and open selection dialog is called
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.Empty)

        verify { activityListener.showImageSelectionDialog() }
    }

    @Test
    fun `Test user click try again`() {
        //Given: we are in error state after being in ImageSelected state
        val bitmap = mockk<Bitmap>(relaxed = true)
        val mediaFile = mockMediaFile(bitmap)
        viewModel.onImageSelected().onMediaFilesPicked(arrayOf(mediaFile), mockk())

        val exception = Exception("")
        viewModel.onImageSelected().onImagePickerError(exception, mockk())

        //When: user taps try again button
        viewModel.onTryAgainClicked()

        //Then: state is back in ImageSelected
        viewModel.imageDetectionStateObserver
            .test()
            .assertValue(ImageDetectionState.ImageSelected(bitmap))

        coVerify { repository.detectFace(any()) }
    }
}