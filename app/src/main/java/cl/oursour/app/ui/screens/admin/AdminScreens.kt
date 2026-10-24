package cl.oursour.app.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.data.local.TrendRow
import cl.oursour.app.ui.components.AppScaffold
import cl.oursour.app.ui.components.EmptyState
import cl.oursour.app.ui.components.SectionTitle
import cl.oursour.app.ui.theme.Amber
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.util.formatClp
import cl.oursour.app.viewmodel.AdminCatalogViewModel
import cl.oursour.app.viewmodel.AdminMetricsViewModel
import cl.oursour.app.viewmodel.AdminProductFormViewModel
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.MetricsRange

// ---------------------------------------------------------------------------
// CU-05: Gestionar catálogo de productos
// ---------------------------------------------------------------------------

@Composable
fun AdminCatalogScreen(
    onNewProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    viewModel: AdminCatalogViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    AppScaffold(
        title = "Catálogo",
        subtitle = "${products.size} productos · ${products.count { it.active }} activos",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo producto") }
            )
        }
    ) { padding ->
        if (products.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    emoji = "🧴",
                    title = "Catálogo vacío",
                    message = "Crea el primer producto con el botón \"Nuevo producto\".",
                    actionLabel = "Nuevo producto",
                    onAction = onNewProduct
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.alpha(if (product.active) 1f else 0.55f),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) { Text(product.emoji, fontSize = 28.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${formatClp(product.price)}  ·  Stock ${product.stock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (product.active) "Activo" else "Desactivado",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (product.active) Lime else MaterialTheme.colorScheme.tertiary
                                )
                            }
                            IconButton(onClick = { onEditProduct(product.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar ${product.name}")
                            }
                            Switch(
                                checked = product.active,
                                onCheckedChange = { viewModel.setActive(product, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Formulario para crear o editar un producto, con validación de datos. */
@Composable
fun AdminProductFormScreen(
    onBack: () -> Unit,
    viewModel: AdminProductFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val emojis = listOf("🍋", "🍹", "🍓", "🍊", "🥒", "🍍", "🍃", "🥂", "🍸")

    AppScaffold(
        title = if (state.isEditing) "Editar producto" else "Nuevo producto",
        subtitle = "Catálogo Our Sour",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = { Text("Nombre comercial") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescription,
                label = { Text("Descripción") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.price,
                    onValueChange = viewModel::onPrice,
                    label = { Text("Precio (CLP)") },
                    isError = state.priceError != null,
                    supportingText = { state.priceError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.stock,
                    onValueChange = viewModel::onStock,
                    label = { Text("Stock simulado") },
                    isError = state.stockError != null,
                    supportingText = { state.stockError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Ícono", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojis.forEach { e ->
                    FilterChip(
                        selected = state.emoji == e,
                        onClick = { viewModel.onEmoji(e) },
                        label = { Text(e, fontSize = 20.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Producto activo", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Si lo desactivas, no se podrá agregar al carro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.active, onCheckedChange = viewModel::onActive)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.save(onSaved = onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (state.isEditing) "Guardar cambios" else "Crear producto", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CU-06: Visualizar métricas de búsquedas y tendencias
// ---------------------------------------------------------------------------

@Composable
fun AdminMetricsScreen(
    viewModel: AdminMetricsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppScaffold(title = "Métricas", subtitle = "Búsquedas y tendencias de los usuarios") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricsRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.range == range,
                        onClick = { viewModel.setRange(range) },
                        label = { Text(range.label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (state.totalSearches == 0 && state.topIngredients.isEmpty() && state.topRecipes.isEmpty()) {
                // Flujo alternativo de CU-06: sin datos en el período.
                EmptyState(
                    emoji = "📊",
                    title = "Sin información",
                    message = "No hay búsquedas registradas en este período."
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Registros", "${state.totalSearches}", Modifier.weight(1f))
                    StatTile("Ingrediente #1", state.topIngredients.firstOrNull()?.name ?: "—", Modifier.weight(1f))
                    StatTile("Receta #1", state.topRecipes.firstOrNull()?.name ?: "—", Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                RankingCard("Ingredientes más buscados", state.topIngredients, Lime)
                Spacer(Modifier.height(16.dp))
                RankingCard("Recetas más consultadas", state.topRecipes, Amber)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 2)
        }
    }
}

/** Lista con barras horizontales proporcionales a la cantidad de búsquedas. */
@Composable
private fun RankingCard(title: String, rows: List<TrendRow>, barColor: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle(title)
            Spacer(Modifier.height(10.dp))
            if (rows.isEmpty()) {
                Text(
                    "Sin datos en este período.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val max = rows.maxOf { it.total }.coerceAtLeast(1)
                rows.forEachIndexed { index, row ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}. ${row.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("${row.total}", style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(row.total / max.toFloat())
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
