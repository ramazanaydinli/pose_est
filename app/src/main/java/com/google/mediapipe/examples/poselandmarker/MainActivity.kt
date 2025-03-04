package com.google.mediapipe.examples.poselandmarker

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        // Get the selected pose
        val selectedPoseOption = intent.getIntExtra("POSE_OPTION", 1)
        Log.d("MainActivity", "Selected pose option: $selectedPoseOption")

        // Wait for layout to be fully initialized before accessing OverlayView
        activityMainBinding.root.post {
            // Access OverlayView through binding
            val overlayView = activityMainBinding.overlayView
            overlayView?.setPoseOption(selectedPoseOption)
            Log.d("MainActivity", "Set pose option to: $selectedPoseOption")
        }

        // Setup navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // Ignore reselection behavior
        }
    }

    override fun onResume() {
        super.onResume()

        // Try setting pose option again in onResume as a fallback
        val selectedPoseOption = intent.getIntExtra("POSE_OPTION", 1)
        findViewById<OverlayView>(R.id.overlayView)?.setPoseOption(selectedPoseOption)
        Log.d("MainActivity", "onResume: Set pose option to: $selectedPoseOption")
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish() // Ensures back button exits the app instead of going back to PoseSelectionActivity
    }
}