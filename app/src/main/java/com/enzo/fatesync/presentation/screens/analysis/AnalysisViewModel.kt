package com.enzo.fatesync.presentation.screens.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzo.fatesync.data.repository.FaceDetector
import com.enzo.fatesync.domain.model.FaceData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalysisState {
    data object Loading : AnalysisState()
    data class FacesDetected(
        val face1: FaceData,
        val face2: FaceData
    ) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faceDetector: FaceDetector
) : ViewModel() {

    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Loading)
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    fun analyzePhotos(photo1UriString: String, photo2UriString: String) {
        viewModelScope.launch {
            _state.value = AnalysisState.Loading

            try {
                val photo1Uri = Uri.parse(photo1UriString)
                val photo2Uri = Uri.parse(photo2UriString)

                val bitmap1 = loadBitmap(photo1Uri)
                val bitmap2 = loadBitmap(photo2Uri)

                if (bitmap1 == null || bitmap2 == null) {
                    _state.value = AnalysisState.Error("Failed to load images")
                    return@launch
                }

                val faces1 = faceDetector.detectFaces(bitmap1)
                val faces2 = faceDetector.detectFaces(bitmap2)

                when {
                    faces1.isEmpty() -> {
                        _state.value = AnalysisState.Error("No face detected in first photo")
                    }
                    faces2.isEmpty() -> {
                        _state.value = AnalysisState.Error("No face detected in second photo")
                    }
                    else -> {
                        _state.value = AnalysisState.FacesDetected(
                            face1 = faces1.first(),
                            face2 = faces2.first()
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = AnalysisState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
