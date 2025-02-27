package com.google.mediapipe.examples.poselandmarker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class PoseSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose_selection)

        val poseOption1Button = findViewById<Button>(R.id.pose_option_1_button)
        val poseOption2Button = findViewById<Button>(R.id.pose_option_2_button)
        val poseOption3Button = findViewById<Button>(R.id.pose_option_3_button)
        val poseOption4Button = findViewById<Button>(R.id.pose_option_4_button)
        val poseOption5Button = findViewById<Button>(R.id.pose_option_5_button)

        // Setting up listeners to send the pose selection to MainActivity
        poseOption1Button.setOnClickListener { startPoseActivity(1) }
        poseOption2Button.setOnClickListener { startPoseActivity(2) }
        poseOption3Button.setOnClickListener { startPoseActivity(3) }
        poseOption4Button.setOnClickListener { startPoseActivity(4) }
        poseOption5Button.setOnClickListener { startPoseActivity(5) }
    }

    private fun startPoseActivity(poseOption: Int) {
        // Set the global variable first
        AppData.selectedPoseOption = poseOption
        Log.d("PoseSelection", "Set global pose option to: $poseOption")

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("POSE_OPTION", poseOption)  // Keep this for backward compatibility
        startActivity(intent)
    }

}
