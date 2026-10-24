package cl.oursour.app.util

import java.security.MessageDigest

/** Utilidad para no guardar contraseñas en texto plano (hash SHA-256). */
object Hasher {
    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
