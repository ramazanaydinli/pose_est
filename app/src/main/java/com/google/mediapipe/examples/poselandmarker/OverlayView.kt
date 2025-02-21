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
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // Hand raise tracking variables
    private var isHandRaised = false
    private var rightHandRaiseCount = 0

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
        rightHandRaiseCount = 0 // Reset count when cleared
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
            // Check if there are any landmarks available
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

                    // Draw lines for the pose landmarks
                    PoseLandmarker.POSE_LANDMARKS.forEach {
                        // Check if landmarks exist at both start and end points before drawing a line
                        if (landmark.size > it.start() && landmark.size > it.end()) {
                            canvas.drawLine(
                                landmark[it.start()].x() * imageWidth * scaleFactor,
                                landmark[it.start()].y() * imageHeight * scaleFactor,
                                landmark[it.end()].x() * imageWidth * scaleFactor,
                                landmark[it.end()].y() * imageHeight * scaleFactor,
                                linePaint
                            )
                        }
                    }
                }

                // Draw count on screen
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 50f
                }
                canvas.drawText("Right Hand Raises: $rightHandRaiseCount", 50f, 100f, textPaint)
            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults
        Log.d("Blazepose Results", results.toString())

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }

        try {
            // Hand raise detection logic
            val landmarksList = results?.landmarks()
            if (landmarksList != null && landmarksList.isNotEmpty() && landmarksList[0].isNotEmpty()) {
                val landmarks = landmarksList[0]
                if (landmarks.size >= 17) {
                    val rightWrist = landmarks[16]
                    val rightElbow = landmarks[14]
                    val rightShoulder = landmarks[12]

                    val wristY = rightWrist.y()
                    val elbowY = rightElbow.y()
                    val shoulderY = rightShoulder.y()

                    // Check if right wrist is above right elbow
                    if (!isHandRaised && wristY < shoulderY) {
                        rightHandRaiseCount++
                        isHandRaised = true
                        Log.d("HandRaise", "Right Hand Raise Count: $rightHandRaiseCount")
                    }

                    if (wristY >= elbowY) {
                        isHandRaised = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "Error processing landmarks", e)
            // Don't throw the exception - just log it and continue
        }

        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}