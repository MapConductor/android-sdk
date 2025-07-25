package com.mapconductor.example.ui.sidebar

import androidx.compose.ui.graphics.vector.ImageVector

data class SidebarItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val isSelected: Boolean = false
)