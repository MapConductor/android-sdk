package com.mapconductor.example.toast

import java.util.UUID

data class ToastMessage(
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val durationMillis: Long = 3000L,
    val onDismiss: () -> Unit
)
