package com.mapconductor.core

import java.time.LocalTime
import android.util.Log

data class Message(
    val text: String,
    val timestamp: String = LocalTime.now()!!.toString(),
)

object MyLogger {
    val messages = mutableListOf<Message>()

    fun debug(text: String) {
        messages.add(Message(text))
    }

    fun printAll() {
        messages.forEach {
            Log.d("debug", "${it.timestamp} - ${it.text}")
        }
    }
}
