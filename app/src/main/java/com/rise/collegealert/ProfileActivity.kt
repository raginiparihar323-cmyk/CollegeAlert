package com.rise.collegealert

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val profileEmail = findViewById<TextView>(R.id.profileEmail)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        profileEmail.text = auth.currentUser?.email ?: "No email found"

        logoutBtn.setOnClickListener {

            auth.signOut()

            Toast.makeText(
                this,
                "Logged out",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}