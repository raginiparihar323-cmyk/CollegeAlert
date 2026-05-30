package com.rise.collegealert

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var eventList: ArrayList<Event>
    private lateinit var filteredList: ArrayList<Event>
    private lateinit var auth: FirebaseAuth

    private val adminEmail = "raginiparihar323@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        val addEventBtn = findViewById<Button>(R.id.addEventBtn)
        val searchBox = findViewById<EditText>(R.id.searchBox)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val currentEmail = auth.currentUser?.email
        val isAdmin = currentEmail == adminEmail

        addEventBtn.visibility = if (isAdmin) View.VISIBLE else View.GONE

        if (!isAdmin) {
            bottomNav.menu.findItem(R.id.nav_add).isVisible = false
            bottomNav.menu.findItem(R.id.nav_dashboard).isVisible = false
        } else {
            bottomNav.menu.findItem(R.id.nav_add).isVisible = true
            bottomNav.menu.findItem(R.id.nav_dashboard).isVisible = true
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        eventList = ArrayList()
        filteredList = ArrayList()

        eventAdapter = EventAdapter(filteredList, isAdmin)
        recyclerView.adapter = eventAdapter

        val database = FirebaseDatabase.getInstance(
            "https://collegealert-d7bed-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )

        val eventsRef = database.getReference("events")

        eventsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()

                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)

                    if (event != null) {
                        event.id = eventSnapshot.key ?: ""
                        eventList.add(event)

                        scheduleReminderForEveryUser(event)
                    }
                }

                filteredList.clear()
                filteredList.addAll(eventList)

                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Firebase Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        searchBox.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchEvents(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        addEventBtn.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.nav_home -> true

                R.id.nav_add -> {
                    if (isAdmin) {
                        startActivity(Intent(this, AddEventActivity::class.java))
                    }
                    true
                }

                R.id.nav_dashboard -> {
                    if (isAdmin) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                    }
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun searchEvents(searchText: String) {
        filteredList.clear()

        val text = searchText.lowercase()

        for (event in eventList) {
            if (
                event.title.lowercase().contains(text) ||
                event.category.lowercase().contains(text) ||
                event.venue.lowercase().contains(text)
            ) {
                filteredList.add(event)
            }
        }

        eventAdapter.notifyDataSetChanged()
    }

    private fun scheduleReminderForEveryUser(event: Event) {
        try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val eventDateTime = format.parse("${event.date} ${event.time}") ?: return

            val reminderTime = eventDateTime.time - (10 * 60 * 1000)
            val delay = reminderTime - System.currentTimeMillis()

            if (delay <= 0) {
                return
            }

            val data = Data.Builder()
                .putString("title", "College Alert")
                .putString("message", "${event.title} starts in 10 minutes!")
                .build()

            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "reminder_${event.id}",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}