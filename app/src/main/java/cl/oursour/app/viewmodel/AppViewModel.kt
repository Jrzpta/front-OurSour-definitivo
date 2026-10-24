package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.session.Session
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Mensaje de una sola vez para mostrar en un Snackbar (con acción opcional). */
data class UiMessage(val text: String, val actionLabel: String? = null)

/** ViewModel global: sesión actual, cantidad de productos en el carro y cierre de sesión. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(private val container: AppContainer) : ViewModel() {

    val session: StateFlow<Session?> = container.session.session

    /** Número de unidades en el carro (se muestra como globito en la barra inferior). */
    val cartCount: StateFlow<Int> = container.session.session
        .flatMapLatest { s ->
            if (s == null) flowOf(0)
            else container.cartRepository.observeCart(s.userId).map { items -> items.sumOf { it.quantity } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    suspend fun awaitSeed() {
        container.seedJob.join()
    }

    fun logout() {
        container.authRepository.logout()
    }
}
