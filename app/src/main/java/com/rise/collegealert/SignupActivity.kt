package com.rise.collegealert

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.signupEmail)
        val password = findViewById<EditText>(R.id.signupPassword)

        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val goToLogin = findViewById<TextView>(R.id.goToLogin)

        signupBtn.setOnClickListener {

            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Signup Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            "Signup Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        goToLogin.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}