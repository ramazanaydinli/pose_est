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

        poseOption1Button.setOnClickListener { val intent = Intent (this, MainActivity::class.java)
        startActivity(intent)}
        poseOption2Button.setOnClickListener { startPoseActivity(2) }
        poseOption3Button.setOnClickListener { startPoseActivity(3) }
        poseOption4Button.setOnClickListener { startPoseActivity(4) }
    }

    private fun startPoseActivity(poseOption: Int) {
        val intent = Intent(this, PoseLandmarkerActivity::class.java)
        intent.putExtra("POSE_OPTION", poseOption)
        startActivity(intent)
    }
}