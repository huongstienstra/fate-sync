package com.enzo.fatesync.domain.model

import android.graphics.Bitmap

data class FaceData(
    val bitmap: Bitmap,
    val boundingBox: FaceBoundingBox,
    val landmarks: List<FaceLandmark>,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val headEulerAngleX: Float = 0f,
    val headEulerAngleY: Float = 0f,
    val headEulerAngleZ: Float = 0f
) {
    val faceWidth: Int get() = boundingBox.right - boundingBox.left
    val faceHeight: Int get() = boundingBox.bottom - boundingBox.top

    fun getLandmark(type: LandmarkType): FaceLandmark? {
        return landmarks.find { it.type == type }
    }

    val eyeDistance: Float?
        get() {
            val leftEye = getLandmark(LandmarkType.LEFT_EYE)
            val rightEye = getLandmark(LandmarkType.RIGHT_EYE)
            return if (leftEye != null && rightEye != null) {
                kotlin.math.sqrt(
                    (rightEye.x - leftEye.x) * (rightEye.x - leftEye.x) +
                    (rightEye.y - leftEye.y) * (rightEye.y - leftEye.y)
                )
            } else null
        }
}

data class FaceBoundingBox(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class FaceLandmark(
    val type: LandmarkType,
    val x: Float,
    val y: Float
)

enum class LandmarkType {
    LEFT_EYE,
    RIGHT_EYE,
    NOSE_TIP,
    LEFT_MOUTH,
    RIGHT_MOUTH,
    LEFT_CHEEK,
    RIGHT_CHEEK
}
