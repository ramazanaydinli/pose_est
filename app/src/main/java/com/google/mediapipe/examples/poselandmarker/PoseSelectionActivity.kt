package com.google.mediapipe.examples.poselandmarker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PoseSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose_selection)

        val poseOption1Button = findViewById<Button>(R.id.pose_option_1_button)
        val poseOption2Button = findViewById<Button>(R.id.pose_option_2_button)
        val poseOption3Button = findViewById<Button>(R.id.pose_option_3_button)
        val poseOption4Button = findViewById<Button>(R.id.pose_option_4_button)

        // Setting up listeners in a cleaner way
        poseOption1Button.setOnClickListener { startPoseActivity(1) }
        poseOption2Button.setOnClickListener { startPoseActivity(2) }
        poseOption3Button.setOnClickListener { startPoseActivity(3) }
        poseOption4Button.setOnClickListener { startPoseActivity(4) }
    }

    private fun startPoseActivity(poseOption: Int) {
        val intent = Intent(this, OverlayView::class.java)
        intent.putExtra("POSE_OPTION", poseOption)
        startActivity(intent)
    }
}
