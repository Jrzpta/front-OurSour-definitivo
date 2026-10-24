package cl.oursour.app.util

/** Cálculo del porcentaje de compatibilidad entre los ingredientes del usuario y una receta. */
object MatchCalculator {
    /** Ej.: tengo 2 de 3 ingredientes -> 66 (%). */
    fun percent(owned: Int, total: Int): Int =
        if (total <= 0) 0 else (owned * 100) / total
}
