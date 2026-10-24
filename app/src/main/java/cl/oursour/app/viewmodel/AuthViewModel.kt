package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.session.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

/** Inicio de sesión y registro. */
class AuthViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.authRepository

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String, onSuccess: (Session) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthUiState(error = "Completa tu correo y contraseña")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val result = repo.login(email, password)
            if (result.isSuccess) {
                _state.value = AuthUiState()
                container.session.current?.let(onSuccess)
            } else {
                _state.value = AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(name: String, email: String, password: String, confirm: String, onSuccess: (Session) -> Unit) {
        if (password != confirm) {
            _state.value = AuthUiState(error = "Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            val result = repo.register(name, email, password)
            if (result.isSuccess) {
                _state.value = AuthUiState()
                container.session.current?.let(onSuccess)
            } else {
                _state.value = AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
