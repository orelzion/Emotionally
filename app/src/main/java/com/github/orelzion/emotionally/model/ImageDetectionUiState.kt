package com.github.orelzion.emotionally.model

import com.github.orelzion.emotionally.model.network.ImageDetectionDetails

sealed class ImageDetectionUiState {
    object Empty : ImageDetectionUiState()
    object Upload : ImageDetectionUiState()
    data class Success(val imageDetectionDetails: ImageDetectionDetails)
    data class Error(val exception: Exception)
}