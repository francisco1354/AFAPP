package com.example.afapp.ui.viewmodel // ⬅️ CORREGIDO

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.local.user.UserEntity // ⬅️ CORREGIDO
import com.example.afapp.data.repository.UserRepository // ⬅️ CORREGIDO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    fun loadUser(email: String) {
        viewModelScope.launch {
            val user = userRepository.findByEmail(email)
            _currentUser.value = user
        }
    }

    fun updateUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun clearSession() {
        _currentUser.value = null
    }
}