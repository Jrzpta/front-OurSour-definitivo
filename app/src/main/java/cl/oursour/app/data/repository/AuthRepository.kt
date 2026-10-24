package cl.oursour.app.data.repository

import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.Roles
import cl.oursour.app.data.local.UserEntity
import cl.oursour.app.data.session.SessionManager
import cl.oursour.app.util.Hasher

/** Inicio de sesión y registro de usuarios (todo local, con datos ficticios). */
class AuthRepository(
    db: AppDatabase,
    private val session: SessionManager
) {
    private val userDao = db.userDao()

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.findByEmail(email.trim().lowercase())
        return if (user != null && user.passwordHash == Hasher.sha256(password)) {
            session.save(user)
            Result.success(user)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        if (name.isBlank()) return Result.failure(Exception("Ingresa tu nombre"))
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(Exception("Ingresa un correo válido"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        if (userDao.findByEmail(cleanEmail) != null) {
            return Result.failure(Exception("Ya existe una cuenta con ese correo"))
        }
        val newUser = UserEntity(
            name = name.trim(),
            email = cleanEmail,
            passwordHash = Hasher.sha256(password),
            role = Roles.CONSUMER
        )
        val id = userDao.insert(newUser)
        val saved = newUser.copy(id = id)
        session.save(saved)
        return Result.success(saved)
    }

    fun logout() = session.clear()
}
