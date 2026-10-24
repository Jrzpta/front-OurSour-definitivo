package cl.oursour.app

import cl.oursour.app.util.Hasher
import cl.oursour.app.util.MatchCalculator
import cl.oursour.app.util.QrParser
import cl.oursour.app.util.QrTarget
import cl.oursour.app.util.formatClp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Pruebas unitarias de la lógica pura (no necesitan emulador). */
class UnitTests {

    @Test
    fun qrParser_recetaValida() {
        assertEquals(QrTarget.Recipe(3), QrParser.parse("OURSOUR:RECIPE:3"))
    }

    @Test
    fun qrParser_productoValido_ignoraMayusculasYEspacios() {
        assertEquals(QrTarget.Product(7), QrParser.parse("  oursour:product:7 "))
    }

    @Test
    fun qrParser_codigoAjeno_esInvalido() {
        assertEquals(QrTarget.Invalid, QrParser.parse("https://ejemplo.cl"))
        assertEquals(QrTarget.Invalid, QrParser.parse("OURSOUR:RECIPE:abc"))
        assertEquals(QrTarget.Invalid, QrParser.parse("OURSOUR:BEBIDA:1"))
    }

    @Test
    fun matchCalculator_porcentajes() {
        assertEquals(100, MatchCalculator.percent(3, 3))
        assertEquals(66, MatchCalculator.percent(2, 3))
        assertEquals(0, MatchCalculator.percent(0, 2))
        assertEquals(0, MatchCalculator.percent(0, 0))
    }

    @Test
    fun formatClp_usaPuntosDeMiles() {
        assertEquals("\$6.990", formatClp(6990))
        assertEquals("\$18.990", formatClp(18990))
        assertEquals("\$500", formatClp(500))
    }

    @Test
    fun hasher_esDeterministaYDistingueTextos() {
        assertEquals(Hasher.sha256("admin123"), Hasher.sha256("admin123"))
        assertNotEquals(Hasher.sha256("admin123"), Hasher.sha256("admin124"))
    }
}
