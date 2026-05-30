package com.rise.collegealert

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class EventAdapter(
    private val eventList: ArrayList<Event>,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLayout: LinearLayout = itemView.findViewById(R.id.cardLayout)
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val eventTitle: TextView = itemView.findViewById(R.id.eventTitleText)
        val eventDetails: TextView = itemView.findViewById(R.id.eventDetailsText)
        val cardView: CardView = itemView.findViewById(R.id.eventCard)
        val editBtn: Button = itemView.findViewById(R.id.editBtn)
        val deleteBtn: Button = itemView.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentEvent = eventList[position]

        holder.categoryText.text = currentEvent.category.uppercase()
        holder.eventTitle.text = currentEvent.title
        holder.eventDetails.text =
            "📅 ${currentEvent.date}   ⏰ ${currentEvent.time}\n📍 ${currentEvent.venue}"

        when (currentEvent.category.lowercase()) {
            "fest" -> holder.cardLayout.setBackgroundColor(Color.parseColor("#FFE5EC"))
            "exam" -> holder.cardLayout.setBackgroundColor(Color.parseColor("#FFF3B0"))
            "workshop" -> holder.cardLayout.setBackgroundColor(Color.parseColor("#D8F3DC"))
            "seminar" -> holder.cardLayout.setBackgroundColor(Color.parseColor("#E0F7FA"))
            "notice" -> holder.cardLayout.setBackgroundColor(Color.parseColor("#EEE6FF"))
            else -> holder.cardLayout.setBackgroundColor(Color.parseColor("#F1FAEE"))
        }

        holder.editBtn.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.deleteBtn.visibility = if (isAdmin) View.VISIBLE else View.GONE

        holder.cardView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EventDetailsActivity::class.java)
            intent.putExtra("title", currentEvent.title)
            intent.putExtra("date", currentEvent.date)
            intent.putExtra("time", currentEvent.time)
            intent.putExtra("venue", currentEvent.venue)
            intent.putExtra("description", currentEvent.description)
            holder.itemView.context.startActivity(intent)
        }

        holder.editBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditEventActivity::class.java)
            intent.putExtra("id", currentEvent.id)
            intent.putExtra("title", currentEvent.title)
            intent.putExtra("date", currentEvent.date)
            intent.putExtra("time", currentEvent.time)
            intent.putExtra("venue", currentEvent.venue)
            intent.putExtra("description", currentEvent.description)
            intent.putExtra("category", currentEvent.category)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteBtn.setOnClickListener {
            val database = FirebaseDatabase.getInstance(
                "https://collegealert-d7bed-default-rtdb.asia-southeast1.firebasedatabase.app/"
            )

            database.getReference("events")
                .child(currentEvent.id)
                .removeValue()

            Toast.makeText(holder.itemView.context, "Event Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}