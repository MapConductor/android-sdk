package com.mapconductor.example.ui.sidebar

data class SidebarItem(
    val id: String,
    val title: String,
)

data class SidebarSection(
    val title: String,
    val items: List<SidebarItem>,
)
