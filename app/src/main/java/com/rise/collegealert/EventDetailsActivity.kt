package com.rise.collegealert

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        val titleText = findViewById<TextView>(R.id.detailsTitle)
        val dateText = findViewById<TextView>(R.id.detailsDate)
        val timeText = findViewById<TextView>(R.id.detailsTime)
        val venueText = findViewById<TextView>(R.id.detailsVenue)
        val descriptionText = findViewById<TextView>(R.id.detailsDescription)
        val calendarBtn = findViewById<Button>(R.id.calendarBtn)

        val title = intent.getStringExtra("title") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val venue = intent.getStringExtra("venue") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        titleText.text = title
        dateText.text = date
        timeText.text = time
        venueText.text = venue
        descriptionText.text = description

        calendarBtn.setOnClickListener {
            try {
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val eventDateTime = format.parse("$date $time")

                if (eventDateTime == null) {
                    Toast.makeText(this, "Invalid date/time", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, title)
                    putExtra(CalendarContract.Events.EVENT_LOCATION, venue)
                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDateTime.time)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventDateTime.time + 60 * 60 * 1000)
                }

                startActivity(calendarIntent)

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Use date format dd/MM/yyyy and time HH:mm",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}