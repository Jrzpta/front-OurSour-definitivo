package cl.oursour.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.ui.theme.Ink
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.viewmodel.AppViewModel
import cl.oursour.app.viewmodel.AppViewModelProvider

/** Perfil: datos de la sesión, accesos rápidos, equipo del proyecto y cierre de sesión. */
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenOrders: () -> Unit,
    onScanQr: () -> Unit,
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val session by appViewModel.session.collectAsStateWithLifecycle()
    val isAdmin = session?.isAdmin == true
    val name = session?.name ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Lime),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1).uppercase().ifBlank { "?" },
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall)
        Text(
            session?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                if (isAdmin) "Administrador / Equipo Comercial" else "Consumidor final",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (!isAdmin) {
            ProfileAction("📦", "Mis pedidos", "Revisa tus compras simuladas", onOpenOrders)
            Spacer(Modifier.height(10.dp))
            ProfileAction("📷", "Escanear QR", "Abre una receta desde un código", onScanQr)
            Spacer(Modifier.height(10.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Acerca de Our Sour App", style = MaterialTheme.typography.titleMedium)
                Text(
                    "MVP académico DSY1105 · Duoc UC · versión 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )
                Text("Equipo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Junior Zapata — Product owner", style = MaterialTheme.typography.bodyMedium)
                Text("Patricio Valenzuela — Developer", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Todos los datos son ficticios y se guardan solo en este teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileAction(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
