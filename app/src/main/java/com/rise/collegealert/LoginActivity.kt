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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val email = findViewById<EditText>(R.id.loginEmail)
        val password = findViewById<EditText>(R.id.loginPassword)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val goToSignup = findViewById<TextView>(R.id.goToSignup)

        loginBtn.setOnClickListener {

            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            "Login Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        goToSignup.setOnClickListener {

            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}