package cl.oursour.app.ui.screens.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.data.local.Categories
import cl.oursour.app.data.repository.RecipeMatch
import cl.oursour.app.ui.components.AppHeader
import cl.oursour.app.ui.components.EmptyState
import cl.oursour.app.ui.components.SectionTitle
import cl.oursour.app.ui.components.recipeBrush
import cl.oursour.app.ui.theme.Coral
import cl.oursour.app.ui.theme.Ink
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.util.formatClp
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.DetailState
import cl.oursour.app.viewmodel.RecipeDetailViewModel

/** CU-03 y CU-04: detalle de una receta, su producto Our Sour y botón "Agregar al carro". */
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onOpenCart: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val result = snackbar.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) onOpenCart()
        }
    }

    val current = state
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (current is DetailState.Success) {
                AddToCartBar(match = current, onAdd = viewModel::addToCart)
            }
        }
    ) { padding ->
        when (current) {
            DetailState.Loading -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is DetailState.Error -> Column(Modifier.padding(padding)) {
                AppHeader(title = "Receta", onBack = onBack)
                // Flujo alternativo de CU-03: mensaje de error + reintentar.
                EmptyState(
                    emoji = "📡",
                    title = "No se pudo cargar",
                    message = current.message,
                    actionLabel = "Reintentar",
                    onAction = viewModel::retry
                )
            }

            is DetailState.Success -> RecipeContent(
                match = current.match,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun AddToCartBar(match: DetailState.Success, onAdd: () -> Unit) {
    val product = match.match.product
    val available = product != null && product.active && product.stock > 0
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        Button(
            onClick = onAdd,
            enabled = available,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(54.dp)
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
            Text(
                text = when {
                    product == null -> "  Sin producto asociado"
                    !available -> "  Producto sin stock"
                    else -> "  Agregar al carro · ${formatClp(product.price)}"
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecipeContent(
    match: RecipeMatch,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = match.recipe
    val product = match.product

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con degradado y emoji grande
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(recipeBrush(recipe.id)),
            contentAlignment = Alignment.Center
        ) {
            Text(recipe.emoji, fontSize = 100.sp)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Ink.copy(alpha = 0.55f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }

        Column(Modifier.padding(20.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = (if (recipe.alcoholic) "🍸 Cóctel" else "🌿 Sin alcohol") +
                    "  ·  ⏱ ${recipe.minutes} min  ·  ${match.percent}% compatible",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(recipe.description, style = MaterialTheme.typography.bodyLarge)

            // Ingredientes
            Spacer(Modifier.height(24.dp))
            SectionTitle("Ingredientes")
            Spacer(Modifier.height(8.dp))
            match.required.forEach { ingredient ->
                val owned = match.missing.none { it.id == ingredient.id }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (owned) Icons.Filled.CheckCircle else Icons.Filled.Close,
                        contentDescription = if (owned) "Lo tienes" else "Te falta",
                        tint = if (owned) Lime else Coral
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            Categories.label(ingredient.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        if (owned) "Lo tienes" else "Te falta",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (owned) Lime else Coral
                    )
                }
            }
            if (product != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(product.emoji, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(product.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text("Producto Our Sour", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }

            // Preparación
            Spacer(Modifier.height(24.dp))
            SectionTitle("Preparación")
            Spacer(Modifier.height(8.dp))
            match.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }

            // Producto asociado
            if (product != null) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Producto asociado")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) { Text(product.emoji, fontSize = 32.sp) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                product.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    formatClp(product.price),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    if (product.stock > 0) "Stock: ${product.stock}" else "Sin stock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
