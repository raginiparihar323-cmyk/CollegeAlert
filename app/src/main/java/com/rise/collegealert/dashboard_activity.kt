package com.rise.collegealert

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var totalEventsText: TextView
    private lateinit var workshopText: TextView
    private lateinit var seminarText: TextView
    private lateinit var festText: TextView
    private lateinit var examText: TextView
    private lateinit var noticeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        totalEventsText = findViewById(R.id.totalEventsText)
        workshopText = findViewById(R.id.workshopText)
        seminarText = findViewById(R.id.seminarText)
        festText = findViewById(R.id.festText)
        examText = findViewById(R.id.examText)
        noticeText = findViewById(R.id.noticeText)

        val database = FirebaseDatabase.getInstance(
            "https://collegealert-d7bed-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )

        database.getReference("events")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    var total = 0
                    var workshops = 0
                    var seminars = 0
                    var fests = 0
                    var exams = 0
                    var notices = 0

                    for (eventSnapshot in snapshot.children) {
                        val event = eventSnapshot.getValue(Event::class.java)

                        if (event != null) {
                            total++

                            when (event.category.lowercase()) {
                                "workshop" -> workshops++
                                "seminar" -> seminars++
                                "fest" -> fests++
                                "exam" -> exams++
                                "notice" -> notices++
                            }
                        }
                    }

                    totalEventsText.text = "Total Events: $total"
                    workshopText.text = "Workshops: $workshops"
                    seminarText.text = "Seminars: $seminars"
                    festText.text = "Fests: $fests"
                    examText.text = "Exams: $exams"
                    noticeText.text = "Notices: $notices"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}