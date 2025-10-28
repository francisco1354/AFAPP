package com.example.afapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.local.storage.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DrawerViewModel(private val userPrefs: UserPreferences) : ViewModel() {

    val theme = userPrefs.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            userPrefs.saveTheme(theme)
        }
    }
}