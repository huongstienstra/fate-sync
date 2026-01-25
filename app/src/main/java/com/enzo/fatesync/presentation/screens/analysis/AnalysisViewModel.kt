package com.enzo.fatesync.presentation.screens.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enzo.fatesync.data.local.LanguageManager
import com.enzo.fatesync.data.remote.GeminiService
import com.enzo.fatesync.data.repository.FaceDetector
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.domain.model.FaceData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "AnalysisViewModel"

enum class AnalysisError {
    NO_FACE_FIRST,
    NO_FACE_SECOND,
    LOAD_IMAGES_FAILED,
    ANALYSIS_FAILED,
    GENERIC_ERROR
}

sealed class AnalysisState {
    data object Loading : AnalysisState()
    data class FacesDetected(
        val face1: FaceData,
        val face2: FaceData,
        val compatibilityResult: CompatibilityResult
    ) : AnalysisState()
    data class Error(val error: AnalysisError) : AnalysisState()
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faceDetector: FaceDetector,
    private val geminiService: GeminiService,
    private val languageManager: LanguageManager
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
                    _state.value = AnalysisState.Error(AnalysisError.LOAD_IMAGES_FAILED)
                    return@launch
                }

                Log.d(TAG, "Starting face detection...")
                val faces1 = faceDetector.detectFaces(bitmap1)
                Log.d(TAG, "Faces detected in photo 1: ${faces1.size}")

                val faces2 = faceDetector.detectFaces(bitmap2)
                Log.d(TAG, "Faces detected in photo 2: ${faces2.size}")

                when {
                    faces1.isEmpty() -> {
                        _state.value = AnalysisState.Error(AnalysisError.NO_FACE_FIRST)
                    }
                    faces2.isEmpty() -> {
                        _state.value = AnalysisState.Error(AnalysisError.NO_FACE_SECOND)
                    }
                    else -> {
                        val face1 = faces1.first()
                        val face2 = faces2.first()

                        // Get current language for Gemini prompt
                        val currentLanguage = languageManager.currentLanguage.first()
                        Log.d(TAG, "Sending face data to Gemini for analysis (language: ${currentLanguage.code})...")
                        val compatibilityResult = geminiService.analyzeCompatibility(face1, face2, currentLanguage.code)
                        Log.d(TAG, "Gemini analysis complete - Score: ${compatibilityResult.overallScore}")

                        _state.value = AnalysisState.FacesDetected(
                            face1 = face1,
                            face2 = face2,
                            compatibilityResult = compatibilityResult
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during analysis", e)
                _state.value = AnalysisState.Error(AnalysisError.GENERIC_ERROR)
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            Log.d(TAG, "Loading bitmap from URI: $uri, scheme: ${uri.scheme}")

            val bitmap = when (uri.scheme) {
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

            if (bitmap != null) {
                val rotation = getRotationFromUri(uri)
                Log.d(TAG, "EXIF rotation for $uri: $rotation degrees")
                if (rotation != 0) {
                    rotateBitmap(bitmap, rotation)
                } else {
                    bitmap
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap", e)
            null
        }
    }

    private fun getRotationFromUri(uri: Uri): Int {
        return try {
            val exif = when (uri.scheme) {
                "file" -> {
                    val path = uri.path ?: return 0
                    ExifInterface(path)
                }
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        ExifInterface(inputStream)
                    } ?: return 0
                }
                else -> return 0
            }

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            Log.d(TAG, "EXIF orientation value: $orientation")

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading EXIF", e)
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
