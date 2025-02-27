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
import kotlin.math.abs
import kotlin.math.sqrt

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var movementCount = 0

    private var isHandRaised = false
    private var isLegRaised = false
    private var isPushUpDown = false
    private var isSitUpDown = false

    private var stretchTimeText = "00:00"






    private var selectedPose: Int = 2 // Default value

    fun setPoseOption(poseOption: Int) {
        Log.d("OverlayView", "Setting pose option to: $poseOption (was: $selectedPose)")
        if (this.selectedPose != poseOption) {
            this.selectedPose = poseOption
            invalidate()
        }
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
                textSize = 100f
            }
            canvas.drawText("Sayı: $movementCount", 50f, 100f, textPaint)

            // Draw stretch time on screen (only show when tracking back stretch)
            if (selectedPose == 5) {
                canvas.drawText("Süre: $stretchTimeText", 50f, 200f, textPaint)
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

                selectedPose = AppData.selectedPoseOption
                Log.d("OverlayView", "Using pose option from AppData: $selectedPose")

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
                    5 ->{
                        Log.d("PoseSelection", "Tracking Back Stretch")
                        trackBackStretch(landmarks)
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
        val rightKnee = landmarks[26]
        val rightHip = landmarks[24]

        val kneeY = rightKnee.y()
        val hipY = rightHip.y()

        // Check if the knee is raised above the hip (pulling to chest)
        if (!isLegRaised && kneeY < hipY) {
            movementCount++
            isLegRaised = true
            Log.d("Movement", "Right Knee Raise Count: $movementCount")
        }

        // Reset when the knee goes back down
        if (kneeY >= hipY) {
            isLegRaised = false
        }
    }


    private fun trackPushUps(landmarks: List<NormalizedLandmark>) {
        val nose = landmarks[0]
        val leftElbow = landmarks[13]
        val rightElbow = landmarks[14]

        val noseY = nose.y()
        val leftElbowY = leftElbow.y()
        val rightElbowY = rightElbow.y()

        // Detect when the nose is below both elbows (down position)
        if (!isPushUpDown && noseY > leftElbowY && noseY > rightElbowY) {
            movementCount++
            isPushUpDown = true
            Log.d("Movement", "Push-Up Count: $movementCount")
        }

        // Reset when the nose moves back up
        if (noseY < leftElbowY && noseY < rightElbowY) {
            isPushUpDown = false
        }
    }


    private fun trackSitUps(landmarks: List<NormalizedLandmark>) {
        val nose = landmarks[0]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftKnee = landmarks.getOrNull(25) // Handle missing landmarks safely
        val rightKnee = landmarks.getOrNull(26)

        val noseY = nose.y()
        val leftHipY = leftHip.y()
        val rightHipY = rightHip.y()

        // Handle cases where one knee is missing (occluded)
        val leftKneeY = leftKnee?.y() ?: rightKnee?.y() ?: leftHipY  // Use right knee if left is missing
        val rightKneeY = rightKnee?.y() ?: leftKnee?.y() ?: rightHipY // Use left knee if right is missing

        // Adjust weight: More weight to knees so threshold is closer to knees
        val leftMidpointY = (leftHipY * 0.2 + leftKneeY * 0.8)  // More knee influence
        val rightMidpointY = (rightHipY * 0.2 + rightKneeY * 0.8)
        val midPointY = (leftMidpointY + rightMidpointY) / 2

        // Debugging logs to check values
        Log.d("SitUpTracking", "NoseY: $noseY, MidPointY: $midPointY, LeftKneeY: $leftKneeY, RightKneeY: $rightKneeY")

        // Detect when the nose moves above the midpoint (sit-up position)
        if (!isSitUpDown && noseY < midPointY) {
            movementCount++
            isSitUpDown = true
            Log.d("Movement", "Sit-Up Count: $movementCount")
        }

        // Reset when the nose moves back down (adjusted for better reset)
        if (noseY > midPointY + 0.02) {  // Reduce buffer for smoother counting
            isSitUpDown = false
        }
    }

    // Class variables
    private var isInBackStretchPosition = false
    private var stretchStartTime = 0L
    private var stretchDurationMs = 0L
    private var totalStretchTimeMs = 0L
    private val MIN_STRETCH_DURATION = 3000L // Minimum 3 seconds to count as a valid stretch

    // Helper function to calculate distance between two landmarks
    private fun calculateDistance(landmark1: NormalizedLandmark, landmark2: NormalizedLandmark): Float {
        val dx = landmark1.x() - landmark2.x()
        val dy = landmark1.y() - landmark2.y()
        return sqrt(dx * dx + dy * dy)
    }

    // For UI updates
    private fun updateStretchTimeUI(timeMs: Long) {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / 1000) / 60

        stretchTimeText = String.format("%02d:%02d", minutes, seconds)
        invalidate() // Request a redraw of the view to show the updated time
    }

    private fun trackBackStretch(landmarks: List<NormalizedLandmark>) {
        // Key landmarks for the stretch position
        val nose = landmarks[0]
        val leftHip = landmarks[23]
        val rightHip = landmarks[24]
        val leftKnee = landmarks.getOrNull(25) ?: return // Left knee must be visible
        val rightKnee = landmarks.getOrNull(26) ?: return // Right knee must be visible
        val leftAnkle = landmarks.getOrNull(27) ?: return // Left ankle must be visible
        val rightAnkle = landmarks.getOrNull(28) ?: return // Right ankle must be visible

        // Position verification
        // 1. Check if legs are raised (ankles higher than hips)
        val legsRaised = (leftAnkle.y() < leftHip.y() && rightAnkle.y() < rightHip.y())

        // 2. Check if right ankle is near left knee
        val ankleToKneeDistance = calculateDistance(rightAnkle, leftKnee)
        val isAnkleOnKnee = ankleToKneeDistance < 0.15 // Threshold for ankle-to-knee proximity

        // 3. Check if person is lying down (nose roughly on same height as hips)
        val hipY = (leftHip.y() + rightHip.y()) / 2
        val isLyingDown = abs(nose.y() - hipY) < 0.1

        // 4. Check if legs are somewhat vertical
        val leftLegVertical = abs(leftAnkle.x() - leftHip.x()) < 0.2
        val rightLegVertical = abs(rightAnkle.x() - rightHip.x()) < 0.2

        // Debugging logs
        Log.d("BackStretchTracking", "LegsRaised: $legsRaised, AnkleOnKnee: $isAnkleOnKnee, " +
                "LyingDown: $isLyingDown, LeftLegVertical: $leftLegVertical, RightLegVertical: $rightLegVertical")

        // Check if all conditions are met for the stretch position
        val isInStretchPosition = legsRaised && isAnkleOnKnee && isLyingDown &&
                (leftLegVertical || rightLegVertical)

        // Track time in position
        val currentTime = System.currentTimeMillis()

        if (isInStretchPosition) {
            if (!isInBackStretchPosition) {
                // Just entered the position
                stretchStartTime = currentTime
                isInBackStretchPosition = true
                Log.d("StretchTracking", "Entered back stretch position")
            }

            // Calculate elapsed time
            stretchDurationMs = currentTime - stretchStartTime
            Log.d("StretchTracking", "Time in stretch: ${stretchDurationMs/1000} seconds")

            // Update UI or notify user about time spent in position
            updateStretchTimeUI(stretchDurationMs)
        } else {
            if (isInBackStretchPosition) {
                // Just exited the position
                isInBackStretchPosition = false
                Log.d("StretchTracking", "Exited back stretch position. Total time: ${stretchDurationMs/1000} seconds")

                // Reset or store completed stretch data
                if (stretchDurationMs >= MIN_STRETCH_DURATION) {
                    totalStretchTimeMs += stretchDurationMs
                    Log.d("StretchTracking", "Stretch completed. Total stretch time: ${totalStretchTimeMs/1000} seconds")
                }

                stretchDurationMs = 0
            }
        }
    }


    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
