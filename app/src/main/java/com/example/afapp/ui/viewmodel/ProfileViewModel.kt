package com.example.afapp.ui.viewmodel // ⬅️ CORREGIDO

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.local.user.UserEntity // ⬅️ CORREGIDO
import com.example.afapp.data.repository.UserRepository // ⬅️ CORREGIDO
import com.example.afapp.utils.ImageUtils // ⬅️ CORREGIDO
import com.example.afapp.utils.PasswordUtils // ⬅️ CORREGIDO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserEntity? = null,
    val error: String? = null,
    val successMsg: String? = null,
    val isEditing: Boolean = false,

    // Campos editables
    val editName: String = "",
    val editPhone: String = "",
    val editPassword: String = "",
    val editConfirmPassword: String = "",
    val editProfileImageUri: String? = null
)

class ProfileViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    fun loadUser(email: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null, successMsg = null) }
            runCatching { repo.findByEmail(email) }
                .onSuccess { user ->
                    _ui.update { s ->
                        s.copy(
                            isLoading = false,
                            user = user,
                            editName = user?.name ?: "",
                            editPhone = user?.phone ?: "",
                            editProfileImageUri = user?.profileImagePath
                        )
                    }
                }
                .onFailure { t ->
                    _ui.update { s ->
                        s.copy(
                            isLoading = false,
                            error = t.message ?: "Error al cargar usuario"
                        )
                    }
                }
        }
    }

    fun startEditing() {
        _ui.update { it.copy(isEditing = true, error = null, successMsg = null) }
    }

    fun cancelEditing() {
        val user = _ui.value.user
        _ui.update {
            it.copy(
                isEditing = false,
                editName = user?.name ?: "",
                editPhone = user?.phone ?: "",
                editPassword = "",
                editConfirmPassword = "",
                editProfileImageUri = user?.profileImagePath,
                error = null
            )
        }
    }

    fun onNameChange(name: String) {
        _ui.update { it.copy(editName = name) }
    }

    fun onPhoneChange(phone: String) {
        val digitsOnly = phone.filter { it.isDigit() }
        _ui.update { it.copy(editPhone = digitsOnly) }
    }

    fun onPasswordChange(password: String) {
        _ui.update { it.copy(editPassword = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _ui.update { it.copy(editConfirmPassword = confirmPassword) }
    }

    fun onProfileImageSelected(uri: String?) {
        _ui.update { it.copy(editProfileImageUri = uri) }
    }

    fun saveChanges(context: Context, onSaveSuccess: (UserEntity) -> Unit) {
        val state = _ui.value
        val user = state.user ?: return

        // Validaciones
        if (state.editName.isBlank()) {
            _ui.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        if (state.editPhone.isBlank() || state.editPhone.length < 8) {
            _ui.update { it.copy(error = "Teléfono inválido (mínimo 8 dígitos)") }
            return
        }

        // Validar contraseña si está intentando cambiarla
        if (state.editPassword.isNotBlank()) {
            if (state.editPassword.length < 8) {
                _ui.update { it.copy(error = "La contraseña debe tener al menos 8 caracteres") }
                return
            }
            if (state.editPassword != state.editConfirmPassword) {
                _ui.update { it.copy(error = "Las contraseñas no coinciden") }
                return
            }
        }

        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null, successMsg = null) }

            // Guardar imagen si cambió
            val savedImagePath = if (state.editProfileImageUri != user.profileImagePath && state.editProfileImageUri != null) {
                if (state.editProfileImageUri.startsWith("content://")) {
                    ImageUtils.saveImageToInternalStorage(context, state.editProfileImageUri.toUri())
                } else {
                    state.editProfileImageUri
                }
            } else {
                user.profileImagePath
            }

            // Actualizar usuario
            val updatedUser = user.copy(
                name = state.editName.trim(),
                phone = state.editPhone.trim(),
                password = if (state.editPassword.isNotBlank()) {
                    PasswordUtils.hashPassword(state.editPassword)
                } else {
                    user.password
                },
                profileImagePath = savedImagePath
            )

            runCatching { repo.update(updatedUser) }
                .onSuccess {
                    _ui.update { s ->
                        s.copy(
                            isLoading = false,
                            user = updatedUser,
                            isEditing = false,
                            editPassword = "",
                            editConfirmPassword = "",
                            successMsg = "Perfil actualizado correctamente"
                        )
                    }
                    onSaveSuccess(updatedUser)
                }
                .onFailure { t ->
                    _ui.update { s ->
                        s.copy(
                            isLoading = false,
                            error = t.message ?: "Error al actualizar"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _ui.update { it.copy(error = null, successMsg = null) }
    }
}