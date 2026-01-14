package com.enzo.fatesync.domain.model

import android.graphics.Bitmap

data class FaceData(
    val bitmap: Bitmap,
    val boundingBox: FaceBoundingBox,
    val landmarks: List<FaceLandmark>
)

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
