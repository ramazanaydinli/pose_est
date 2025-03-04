package com.google.mediapipe.examples.poselandmarker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val etAge = findViewById<EditText>(R.id.etAge)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val age = etAge.text.toString().trim()
            val height = etHeight.text.toString().trim()
            val weight = etWeight.text.toString().trim()

            val selectedGenderId = radioGroupGender.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                else -> ""
            }

            if (age.isEmpty() || height.isEmpty() || weight.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate to the main activity (or another screen)
            val intent = Intent(this, PoseSelectionActivity::class.java).apply {
                putExtra("AGE", age)
                putExtra("HEIGHT", height)
                putExtra("WEIGHT", weight)
                putExtra("GENDER", gender)
            }
            startActivity(intent)
            finish() // Close the entry screen
        }
    }
}
