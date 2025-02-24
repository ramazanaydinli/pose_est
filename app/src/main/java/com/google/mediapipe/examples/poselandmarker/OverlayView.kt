package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark  // Correct NormalizedLandmark import
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var selectedPose: Int = 1
    private var movementCount = 0

    private var isHandRaised = false
    private var isLegRaised = false
    private var isPushUpDown = false
    private var isSitUpDown = false



    fun setPoseSelection(pose: Int) {

        Log.d("PoseSelectionTop", "Setting Pose to: $pose")  // Log to see the input pose
        selectedPose = pose
        movementCount = 0  // Reset count when switching movement
        invalidate()  // Triggers the redraw
    }


    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
        movementCount = 0 // Reset count when cleared
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            val landmarksList = poseLandmarkerResult.landmarks()
            if (landmarksList.isNotEmpty()) {
                for (landmark in landmarksList) {
                    for (normalizedLandmark in landmark) {
                        canvas.drawPoint(
                            normalizedLandmark.x() * imageWidth * scaleFactor,
                            normalizedLandmark.y() * imageHeight * scaleFactor,
                            pointPaint
                        )
                    }

                }
            }

            // Draw count on screen
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 50f
            }
            canvas.drawText("Count: $movementCount", 50f, 100f, textPaint)
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }

        try {
            val landmarksList = results?.landmarks()
            if (landmarksList != null && landmarksList.isNotEmpty() && landmarksList[0].isNotEmpty()) {
                val landmarks = landmarksList[0]

                Log.d("PoseSelection", "Current Pose: $selectedPose")  // Log the selectedPose here
                Log.d("PoseSelection", "Landmarks size: ${landmarks.size}")  // Log landmarks size for debugging


                when (selectedPose) {
                    1 -> {
                        Log.d("PoseSelection", "Tracking Left Hand Raise")
                        trackLeftHandRaise(landmarks)
                    }
                    2 -> {
                        Log.d("PoseSelection", "Tracking Right Leg Raise")
                        trackRightLegRaise(landmarks)
                    }
                    3 -> {
                        Log.d("PoseSelection", "Tracking Push-Ups")
                        trackPushUps(landmarks)
                    }
                    4 -> {
                        Log.d("PoseSelection", "Tracking Sit-Ups")
                        trackSitUps(landmarks)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "Error processing landmarks", e)
        }

        invalidate()
    }

    // Updated to accept List<NormalizedLandmark> as input instead of Landmark
    private fun trackLeftHandRaise(landmarks: List<NormalizedLandmark>) {
        Log.d("Tracking", "Tracking Left Hand Raise")
        val leftWrist = landmarks[15]
        val leftElbow = landmarks[13]
        val leftShoulder = landmarks[11]

        val wristY = leftWrist.y()
        val elbowY = leftElbow.y()
        val shoulderY = leftShoulder.y()

        if (!isHandRaised && wristY < shoulderY) {
            movementCount++
            isHandRaised = true
            Log.d("Movement", "Left Hand Raise Count: $movementCount")
        }

        if (wristY >= elbowY) {
            isHandRaised = false
        }
    }

    private fun trackRightLegRaise(landmarks: List<NormalizedLandmark>) {
        val rightAnkle = landmarks[28]
        val rightKnee = landmarks[26]
        val rightHip = landmarks[24]

        val ankleY = rightAnkle.y()
        val kneeY = rightKnee.y()
        val hipY = rightHip.y()

        if (!isLegRaised && ankleY < kneeY) {
            movementCount++
            isLegRaised = true
            Log.d("Movement", "Right Leg Raise Count: $movementCount")
        }

        if (ankleY >= hipY) {
            isLegRaised = false
        }
    }

    private fun trackPushUps(landmarks: List<NormalizedLandmark>) {
        val nose = landmarks[0]
        val leftWrist = landmarks[15]
        val rightWrist = landmarks[16]

        val noseY = nose.y()
        val leftWristY = leftWrist.y()
        val rightWristY = rightWrist.y()

        if (!isPushUpDown && noseY > leftWristY && noseY > rightWristY) {
            movementCount++
            isPushUpDown = true
            Log.d("Movement", "Push-Up Count: $movementCount")
        }

        if (noseY < leftWristY && noseY < rightWristY) {
            isPushUpDown = false
        }
    }

    private fun trackSitUps(landmarks: List<NormalizedLandmark>) {
        val nose = landmarks[0]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]

        val noseY = nose.y()
        val leftHipY = leftHip.y()
        val rightHipY = rightHip.y()

        if (!isSitUpDown && noseY < leftHipY && noseY < rightHipY) {
            movementCount++
            isSitUpDown = true
            Log.d("Movement", "Sit-Up Count: $movementCount")
        }

        if (noseY > leftHipY && noseY > rightHipY) {
            isSitUpDown = false
        }
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
