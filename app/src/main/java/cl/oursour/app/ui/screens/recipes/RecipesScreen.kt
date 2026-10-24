package cl.oursour.app.ui.screens.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.ui.components.AppScaffold
import cl.oursour.app.ui.components.EmptyState
import cl.oursour.app.ui.components.RecipeCard
import cl.oursour.app.ui.components.recipeBrush
import cl.oursour.app.util.formatClp
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.RecipeFilter
import cl.oursour.app.viewmodel.RecipesViewModel

/** CU-02: recetas compatibles con los ingredientes declarados. */
@Composable
fun RecipesScreen(
    onOpenRecipe: (Long) -> Unit,
    onDeclareIngredients: () -> Unit,
    onScanQr: () -> Unit,
    viewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Al entrar a la pantalla se registra la búsqueda (alimenta las métricas del admin).
    LaunchedEffect(Unit) { viewModel.logSearch() }

    AppScaffold(
        title = "Recetas para ti",
        subtitle = if (state.ownedCount == 0) "Aún no declaras ingredientes"
        else "Según tus ${state.ownedCount} ingredientes"
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Filtros
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecipeFilter.entries.forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mostrar solo compatibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state.onlyCompatible, onCheckedChange = viewModel::setOnlyCompatible)
            }

            if (state.results.isEmpty() && !state.loading) {
                NoResults(
                    ownedCount = state.ownedCount,
                    state = state,
                    onDeclareIngredients = onDeclareIngredients,
                    onShowAll = { viewModel.setOnlyCompatible(false) },
                    onScanQr = onScanQr
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.results, key = { it.recipe.id }) { match ->
                        RecipeCard(match = match, onClick = { onOpenRecipe(match.recipe.id) })
                    }
                }
            }
        }
    }
}

/** Flujo alternativo de CU-02: no hay coincidencias -> mensaje y productos Our Sour sugeridos. */
@Composable
private fun NoResults(
    ownedCount: Int,
    state: cl.oursour.app.viewmodel.RecipesUiState,
    onDeclareIngredients: () -> Unit,
    onShowAll: () -> Unit,
    onScanQr: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyState(
            emoji = if (ownedCount == 0) "🧺" else "🤔",
            title = if (ownedCount == 0) "Declara tus ingredientes" else "Sin coincidencias",
            message = if (ownedCount == 0)
                "Cuéntanos qué tienes en casa para sugerirte recetas que puedes preparar."
            else
                "No encontramos recetas compatibles con lo que tienes. Prueba con otros ingredientes.",
            actionLabel = "Ir a mis ingredientes",
            onAction = onDeclareIngredients
        )
        OutlinedButton(onClick = onShowAll) { Text("Ver todas las recetas") }

        if (state.suggestedProducts.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Productos Our Sour que te pueden interesar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            state.suggestedProducts.forEach { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(recipeBrush(product.id)),
                            contentAlignment = Alignment.Center
                        ) { Text(product.emoji, fontSize = 26.sp) }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                product.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Text(formatClp(product.price), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onScanQr) { Text("📷  Escanear un QR") }
    }
}
