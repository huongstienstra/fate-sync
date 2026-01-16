package com.enzo.fatesync.presentation.screens.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import java.io.File
import javax.inject.Inject

private const val TAG = "AnalysisViewModel"

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
                Log.d(TAG, "Photo1 URI: $photo1UriString")
                Log.d(TAG, "Photo2 URI: $photo2UriString")

                val decodedUri1 = Uri.decode(photo1UriString)
                val decodedUri2 = Uri.decode(photo2UriString)

                Log.d(TAG, "Decoded Photo1 URI: $decodedUri1")
                Log.d(TAG, "Decoded Photo2 URI: $decodedUri2")

                val photo1Uri = Uri.parse(decodedUri1)
                val photo2Uri = Uri.parse(decodedUri2)

                val bitmap1 = loadBitmap(photo1Uri)
                val bitmap2 = loadBitmap(photo2Uri)

                Log.d(TAG, "Bitmap1 loaded: ${bitmap1 != null}, size: ${bitmap1?.width}x${bitmap1?.height}")
                Log.d(TAG, "Bitmap2 loaded: ${bitmap2 != null}, size: ${bitmap2?.width}x${bitmap2?.height}")

                if (bitmap1 == null || bitmap2 == null) {
                    _state.value = AnalysisState.Error("Failed to load images. Bitmap1: ${bitmap1 != null}, Bitmap2: ${bitmap2 != null}")
                    return@launch
                }

                Log.d(TAG, "Starting face detection...")
                val faces1 = faceDetector.detectFaces(bitmap1)
                Log.d(TAG, "Faces detected in photo 1: ${faces1.size}")

                val faces2 = faceDetector.detectFaces(bitmap2)
                Log.d(TAG, "Faces detected in photo 2: ${faces2.size}")

                when {
                    faces1.isEmpty() -> {
                        _state.value = AnalysisState.Error("No face detected in first photo. Make sure face is clearly visible.")
                    }
                    faces2.isEmpty() -> {
                        _state.value = AnalysisState.Error("No face detected in second photo. Make sure face is clearly visible.")
                    }
                    else -> {
                        _state.value = AnalysisState.FacesDetected(
                            face1 = faces1.first(),
                            face2 = faces2.first()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during analysis", e)
                _state.value = AnalysisState.Error("Error: ${e.message}")
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            Log.d(TAG, "Loading bitmap from URI: $uri, scheme: ${uri.scheme}")

            when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path ?: return null)
                    Log.d(TAG, "File exists: ${file.exists()}, path: ${file.absolutePath}")
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown URI scheme: ${uri.scheme}")
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap", e)
            null
        }
    }
}
