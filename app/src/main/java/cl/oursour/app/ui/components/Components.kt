package cl.oursour.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.oursour.app.data.repository.RecipeMatch
import cl.oursour.app.ui.theme.Amber
import cl.oursour.app.ui.theme.Coral
import cl.oursour.app.ui.theme.Ink
import cl.oursour.app.ui.theme.Lime

// ---------------------------------------------------------------------------
// Fondos y degradados de marca
// ---------------------------------------------------------------------------

val BrandBackground: Brush = Brush.verticalGradient(
    listOf(Color(0xFF0E1512), Color(0xFF16261D), Color(0xFF0E1512))
)

private val recipeGradients = listOf(
    listOf(Color(0xFF2F6B2A), Color(0xFFC6F24E)),
    listOf(Color(0xFFB3541E), Color(0xFFF4B942)),
    listOf(Color(0xFF8E2F4F), Color(0xFFFF7A59)),
    listOf(Color(0xFF1F5D6B), Color(0xFF7BE0C3)),
    listOf(Color(0xFF5B3A8E), Color(0xFFE08BE7)),
    listOf(Color(0xFF6B5A1F), Color(0xFFF2E25C))
)

/** Cada receta tiene un degradado distinto (según su id) para que la lista se vea viva. */
fun recipeBrush(id: Long): Brush {
    val colors = recipeGradients[(id % recipeGradients.size).toInt().coerceAtLeast(0)]
    return Brush.linearGradient(colors)
}

// ---------------------------------------------------------------------------
// Estructura de pantalla
// ---------------------------------------------------------------------------

/** Encabezado de pantalla (título, subtítulo, botón volver y acciones). */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        actions()
    }
}

/** Scaffold común: encabezado arriba, Snackbar y contenido. */
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { AppHeader(title = title, subtitle = subtitle, onBack = onBack, actions = actions) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = content
    )
}

// ---------------------------------------------------------------------------
// Componentes reutilizables
// ---------------------------------------------------------------------------

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** Tarjeta de receta con barra de compatibilidad (CU-02). */
@Composable
fun RecipeCard(
    match: RecipeMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = match.recipe
    val barColor = when {
        match.percent >= 100 -> Lime
        match.percent >= 50 -> Amber
        else -> Coral
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(recipeBrush(recipe.id)),
                contentAlignment = Alignment.Center
            ) {
                Text(recipe.emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (if (recipe.alcoholic) "🍸 Cóctel" else "🌿 Sin alcohol") + "  ·  ⏱ ${recipe.minutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { match.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (match.missing.isEmpty()) "¡Tienes todo para prepararlo!"
                    else "Te falta: " + match.missing.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (match.missing.isEmpty()) Lime else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "${match.percent}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
    }
}

/** Texto oscuro sobre fondos de color de marca. */
val OnBrand: Color = Ink
