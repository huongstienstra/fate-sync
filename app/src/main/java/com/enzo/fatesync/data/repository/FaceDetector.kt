package com.enzo.fatesync.data.repository

import android.graphics.Bitmap
import com.enzo.fatesync.domain.model.FaceBoundingBox
import com.enzo.fatesync.domain.model.FaceData
import com.enzo.fatesync.domain.model.FaceLandmark
import com.enzo.fatesync.domain.model.LandmarkType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark as MLKitLandmark
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceDetector @Inject constructor() {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    suspend fun detectFaces(bitmap: Bitmap): List<FaceData> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faces = detector.process(inputImage).await()

        return faces.map { face: Face ->
            FaceData(
                bitmap = bitmap,
                boundingBox = face.toBoundingBox(),
                landmarks = face.extractLandmarks(),
                smilingProbability = face.smilingProbability,
                leftEyeOpenProbability = face.leftEyeOpenProbability,
                rightEyeOpenProbability = face.rightEyeOpenProbability,
                headEulerAngleX = face.headEulerAngleX,
                headEulerAngleY = face.headEulerAngleY,
                headEulerAngleZ = face.headEulerAngleZ
            )
        }
    }

    private fun Face.toBoundingBox(): FaceBoundingBox {
        return FaceBoundingBox(
            left = boundingBox.left,
            top = boundingBox.top,
            right = boundingBox.right,
            bottom = boundingBox.bottom
        )
    }

    private fun Face.extractLandmarks(): List<FaceLandmark> {
        val landmarks = mutableListOf<FaceLandmark>()

        getLandmark(MLKitLandmark.LEFT_EYE)?.let {
            landmarks.add(FaceLandmark(LandmarkType.LEFT_EYE, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.RIGHT_EYE)?.let {
            landmarks.add(FaceLandmark(LandmarkType.RIGHT_EYE, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.NOSE_BASE)?.let {
            landmarks.add(FaceLandmark(LandmarkType.NOSE_TIP, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.MOUTH_LEFT)?.let {
            landmarks.add(FaceLandmark(LandmarkType.LEFT_MOUTH, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.MOUTH_RIGHT)?.let {
            landmarks.add(FaceLandmark(LandmarkType.RIGHT_MOUTH, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.LEFT_CHEEK)?.let {
            landmarks.add(FaceLandmark(LandmarkType.LEFT_CHEEK, it.position.x, it.position.y))
        }

        getLandmark(MLKitLandmark.RIGHT_CHEEK)?.let {
            landmarks.add(FaceLandmark(LandmarkType.RIGHT_CHEEK, it.position.x, it.position.y))
        }

        return landmarks
    }
}
