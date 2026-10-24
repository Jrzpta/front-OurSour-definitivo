package cl.oursour.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.oursour.app.ui.navigation.OurSourNavHost
import cl.oursour.app.ui.theme.OurSourTheme

/** Única Activity de la app: todas las pantallas son Composables dentro de ella. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            OurSourTheme {
                OurSourNavHost()
            }
        }
    }
}
