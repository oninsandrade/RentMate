package com.example.rentmate

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    var isDarkMode = mutableStateOf(false)

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
    }
}
