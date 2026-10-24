package cl.oursour.app.data.session

import android.content.Context
import cl.oursour.app.data.local.Roles
import cl.oursour.app.data.local.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Datos mínimos del usuario que tiene la sesión iniciada. */
data class Session(
    val userId: Long,
    val name: String,
    val email: String,
    val role: String
) {
    val isAdmin: Boolean get() = role == Roles.ADMIN
}

/**
 * Guarda la sesión en SharedPreferences para que, al cerrar y abrir la app,
 * el usuario siga conectado. Expone la sesión como StateFlow para que la UI reaccione.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("oursour_session", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(load())
    val session: StateFlow<Session?> = _session.asStateFlow()

    /** Sesión actual (o null si no hay nadie conectado). */
    val current: Session? get() = _session.value

    private fun load(): Session? {
        val id = prefs.getLong(KEY_ID, -1L)
        if (id < 0) return null
        return Session(
            userId = id,
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = prefs.getString(KEY_ROLE, Roles.CONSUMER) ?: Roles.CONSUMER
        )
    }

    fun save(user: UserEntity) {
        prefs.edit()
            .putLong(KEY_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_ROLE, user.role)
            .apply()
        _session.value = Session(user.id, user.name, user.email, user.role)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private companion object {
        const val KEY_ID = "userId"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
    }
}
