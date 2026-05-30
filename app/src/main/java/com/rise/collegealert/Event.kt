package com.rise.collegealert

data class Event(
    var id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = "",
    val description: String = "",
    val category: String = ""
)