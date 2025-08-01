package com.mapconductor.example.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class NavigationViewModel : ViewModel() {
    private val _currentPage = mutableStateOf("circle")
    val currentPage: State<String> = _currentPage

    private val _isSidebarExpanded = mutableStateOf(false)
    val isSidebarExpanded: State<Boolean> = _isSidebarExpanded

    fun navigateTo(pageId: String) {
        _currentPage.value = pageId
    }

    fun toggleSidebar() {
        _isSidebarExpanded.value = !_isSidebarExpanded.value
    }
}
