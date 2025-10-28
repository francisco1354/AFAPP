package com.example.afapp.ui.viewmodel // ⬅️ CORREGIDO

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.repository.UserRepository
import com.example.afapp.domain.validation.validateConfirm
import com.example.afapp.domain.validation.validateEmail
import com.example.afapp.domain.validation.validateNameLettersOnly
import com.example.afapp.domain.validation.validatePhoneDigitsOnly
import com.example.afapp.domain.validation.validateStrongPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------- ESTADOS DE UI -----------------
data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",

    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// ----------------- VIEWMODEL -----------------
class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    // ---------- LOGIN ----------
    fun onLoginEmailChange(value: String) {
        _login.update {
            // Validamos email en tiempo real
            it.copy(email = value, emailError = validateEmail(value))
        }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update {
            // Solo actualizamos el valor y forzamos la revalidación en submit
            it.copy(pass = value)
        }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        // Solo chequeamos que email sea válido (no nulo) y ambos campos no estén vacíos
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value

        // VALIDACIÓN DE CONTRASEÑA FORZADA ANTES DEL ENVÍO
        val passError = validateStrongPassword(s.pass)

        if (passError != null) {
            // Si hay error en el formato de contraseña, actualizamos el estado y salimos
            _login.update {
                it.copy(
                    passError = passError,
                    canSubmit = false,
                    errorMsg = "Corrige la contraseña"
                )
            }
            return
        }

        // Si ya hay un error de email, o no se puede enviar por cualquier otra razón
        if (s.emailError != null || !s.canSubmit || s.isSubmitting) return

        // Aseguramos que el passError se limpie si ya se corrigió
        _login.update { it.copy(passError = null, errorMsg = null) }

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            delay(500) // ⏳ Simulamos carga

            val result = runCatching {
                repository.login(s.email.trim(), s.pass)
            }.fold(
                onSuccess = { it },
                onFailure = { Result.failure(it) }
            )

            _login.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        // El mensaje vendrá del UserRepository: "Credenciales inválidas"
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación desconocido"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    fun resetLoginForm() {
        _login.value = LoginUiState()
    }

    // ---------- REGISTRO ----------
    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNameLettersOnly(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() &&
                s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            delay(700) // ⏳ Simulamos carga de registro

            val result = runCatching {
                repository.register(
                    name = s.name.trim(),
                    email = s.email.trim(),
                    phone = s.phone.trim(),
                    password = s.pass
                )
            }.fold(
                onSuccess = { it },
                onFailure = { Result.failure(it) }
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    fun resetRegisterForm() {
        _register.value = RegisterUiState()
    }
}