package com.github.orelzion.emotionally.model

interface ActivityActionsListener {
    fun requestPermissionCheck(requestedPermission: String)
    fun showImageSelectionDialog()
    fun openImageSource(chooseImageRequest: ChooseImageRequest)
}