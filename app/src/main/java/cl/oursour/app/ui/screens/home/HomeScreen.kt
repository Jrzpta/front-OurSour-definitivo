package cl.oursour.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.ui.components.SectionTitle
import cl.oursour.app.ui.components.recipeBrush
import cl.oursour.app.ui.theme.Amber
import cl.oursour.app.ui.theme.Ink
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onSeeRecipes: () -> Unit,
    onDeclareIngredients: () -> Unit,
    onScanQr: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ownedCount by viewModel.ownedCount.collectAsStateWithLifecycle()
    val featured by viewModel.featured.collectAsStateWithLifecycle()
    val firstName = viewModel.userName.substringBefore(" ").ifBlank { "bartender" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Saludo
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Hola, $firstName 👋",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "¿Qué vamos a preparar hoy?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        // Tarjeta principal (héroe)
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF2F6B2A), Lime)),
                    shape = MaterialTheme.shapes.large
                )
                .padding(22.dp)
        ) {
            Column {
                Text("🍸", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (ownedCount == 0) "Cuéntanos qué tienes en casa"
                    else "Tienes $ownedCount ingredientes declarados",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Text(
                    if (ownedCount == 0) "Declara tus ingredientes y te sugerimos cócteles al instante."
                    else "Te mostramos las recetas que puedes preparar ahora mismo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = if (ownedCount == 0) onDeclareIngredients else onSeeRecipes,
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Lime)
                ) {
                    Text(
                        if (ownedCount == 0) "Declarar ingredientes" else "Ver recetas para mí",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Accesos rápidos
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAction("🧂", "Mis ingredientes", onDeclareIngredients, Modifier.weight(1f))
            QuickAction("📷", "Escanear QR", onScanQr, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Recomendadas
        SectionTitle("Recomendadas para ti", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(featured, key = { it.recipe.id }) { match ->
                Card(
                    onClick = { onOpenRecipe(match.recipe.id) },
                    modifier = Modifier.width(160.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(recipeBrush(match.recipe.id)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(match.recipe.emoji, fontSize = 46.sp)
                    }
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            match.recipe.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${match.percent}% compatible",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (match.percent > 0) Lime else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Banner inferior
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("De la inspiración al carro", style = MaterialTheme.typography.titleMedium, color = Amber)
                Text(
                    "Elige una receta, mira el producto Our Sour que la acompaña y agrégalo a tu carro sin salir de la app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onSeeRecipes) { Text("Explorar recetas") }
            }
        }
    }
}

@Composable
private fun QuickAction(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}
