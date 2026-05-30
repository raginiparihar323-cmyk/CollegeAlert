package com.rise.collegealert

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class EditEventActivity : AppCompatActivity() {

    private val selectedCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        val title = findViewById<EditText>(R.id.editTitle)
        val date = findViewById<EditText>(R.id.editDate)
        val time = findViewById<EditText>(R.id.editTime)
        val venue = findViewById<EditText>(R.id.editVenue)
        val category = findViewById<AutoCompleteTextView>(R.id.editCategory)
        val description = findViewById<EditText>(R.id.editDescription)
        val updateBtn = findViewById<Button>(R.id.updateBtn)

        val categories = arrayOf("Workshop", "Seminar", "Fest", "Exam", "Notice")
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categories
        )

        category.setAdapter(categoryAdapter)
        category.setOnClickListener {
            category.showDropDown()
        }

        val eventId = intent.getStringExtra("id") ?: ""

        title.setText(intent.getStringExtra("title"))
        date.setText(intent.getStringExtra("date"))
        time.setText(intent.getStringExtra("time"))
        venue.setText(intent.getStringExtra("venue"))
        category.setText(intent.getStringExtra("category"), false)
        description.setText(intent.getStringExtra("description"))

        date.setOnClickListener {
            showDatePicker(date)
        }

        time.setOnClickListener {
            showTimePicker(time)
        }

        updateBtn.setOnClickListener {

            if (
                title.text.toString().isEmpty() ||
                date.text.toString().isEmpty() ||
                time.text.toString().isEmpty() ||
                venue.text.toString().isEmpty() ||
                category.text.toString().isEmpty() ||
                description.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedEvent = Event(
                eventId,
                title.text.toString(),
                date.text.toString(),
                time.text.toString(),
                venue.text.toString(),
                description.text.toString(),
                category.text.toString()
            )

            val database = FirebaseDatabase.getInstance(
                "https://collegealert-d7bed-default-rtdb.asia-southeast1.firebasedatabase.app/"
            )

            database.getReference("events")
                .child(eventId)
                .setValue(updatedEvent)
                .addOnSuccessListener {

                    scheduleReminder(
                        title.text.toString(),
                        date.text.toString(),
                        time.text.toString(),
                        10
                    )

                    Toast.makeText(
                        this,
                        "Event Updated & Reminder Scheduled",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Update Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showDatePicker(dateField: EditText) {
        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                selectedCalendar.set(Calendar.YEAR, selectedYear)
                selectedCalendar.set(Calendar.MONTH, selectedMonth)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateField.setText(format.format(selectedCalendar.time))
            },
            year,
            month,
            day
        ).show()
    }

    private fun showTimePicker(timeField: EditText) {
        val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedCalendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedCalendar.set(Calendar.MINUTE, selectedMinute)

                val formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    selectedHour,
                    selectedMinute
                )

                timeField.setText(formattedTime)
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun scheduleReminder(
        eventTitle: String,
        eventDate: String,
        eventTime: String,
        minutesBefore: Int
    ) {
        try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val eventDateTime = format.parse("$eventDate $eventTime")

            if (eventDateTime == null) {
                Toast.makeText(this, "Invalid date/time", Toast.LENGTH_SHORT).show()
                return
            }

            val reminderTime = eventDateTime.time - (minutesBefore * 60 * 1000)
            val delay = reminderTime - System.currentTimeMillis()

            if (delay <= 0) {
                Toast.makeText(this, "Reminder time already passed", Toast.LENGTH_SHORT).show()
                return
            }

            val data = Data.Builder()
                .putString("title", "College Alert")
                .putString("message", "$eventTitle starts in $minutesBefore minutes!")
                .build()

            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(reminderWork)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Date/time error",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}