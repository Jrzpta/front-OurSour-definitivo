package cl.oursour.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 6990 -> "$6.990" (formato peso chileno, sin depender del idioma del teléfono). */
fun formatClp(amount: Int): String {
    val sign = if (amount < 0) "-" else ""
    val digits = Math.abs(amount).toString().reversed().chunked(3).joinToString(".").reversed()
    return sign + "\$" + digits
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))
