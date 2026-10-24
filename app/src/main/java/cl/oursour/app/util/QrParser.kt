package cl.oursour.app.util

/** Resultado de interpretar el texto de un código QR. */
sealed interface QrTarget {
    data class Recipe(val id: Long) : QrTarget
    data class Product(val id: Long) : QrTarget
    data object Invalid : QrTarget
}

/**
 * Formato de los QR de Our Sour:
 *   OURSOUR:RECIPE:<id>   -> abre el detalle de una receta
 *   OURSOUR:PRODUCT:<id>  -> abre la receta asociada a un producto
 */
object QrParser {
    private const val PREFIX = "OURSOUR"

    fun parse(raw: String): QrTarget {
        val parts = raw.trim().split(":")
        if (parts.size != 3 || !parts[0].equals(PREFIX, ignoreCase = true)) return QrTarget.Invalid
        val id = parts[2].trim().toLongOrNull() ?: return QrTarget.Invalid
        return when (parts[1].trim().uppercase()) {
            "RECIPE" -> QrTarget.Recipe(id)
            "PRODUCT" -> QrTarget.Product(id)
            else -> QrTarget.Invalid
        }
    }
}
