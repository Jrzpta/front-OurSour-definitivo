package cl.oursour.app.ui.screens.cart

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.data.local.OrderStatus
import cl.oursour.app.ui.components.AppScaffold
import cl.oursour.app.ui.components.EmptyState
import cl.oursour.app.util.formatClp
import cl.oursour.app.util.formatDate
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.CartEvent
import cl.oursour.app.viewmodel.CartViewModel
import cl.oursour.app.viewmodel.OrdersViewModel

/** CU-04: carro de compras (estado "En preparación"). */
@Composable
fun CartScreen(
    onExploreRecipes: () -> Unit,
    onOpenOrders: () -> Unit,
    viewModel: CartViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var orderedId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.Message -> snackbar.showSnackbar(event.text)
                is CartEvent.Ordered -> orderedId = event.orderId
            }
        }
    }

    AppScaffold(
        title = "Mi carro",
        subtitle = if (state.items.isEmpty()) "Estado: sin productos" else "Estado: ${OrderStatus.EN_PREPARACION}",
        snackbarHostState = snackbar,
        actions = { TextButton(onClick = onOpenOrders) { Text("Mis pedidos") } },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Total (${state.count} ${if (state.count == 1) "unidad" else "unidades"})",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatClp(state.total),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::checkout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text("Solicitar pedido (simulado)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    emoji = "🛒",
                    title = "Tu carro está vacío",
                    message = "Elige una receta y agrega el producto Our Sour que la acompaña.",
                    actionLabel = "Explorar recetas",
                    onAction = onExploreRecipes
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    Card(
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
                            ) { Text("🍋", fontSize = 28.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${formatClp(item.unitPrice)} c/u",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.changeQuantity(item, -1) },
                                        modifier = Modifier.size(34.dp)
                                    ) { Text("−", fontSize = 18.sp) }
                                    Text(
                                        "${item.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                    FilledTonalIconButton(
                                        onClick = { viewModel.changeQuantity(item, 1) },
                                        modifier = Modifier.size(34.dp)
                                    ) { Text("+", fontSize = 18.sp) }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    formatClp(item.unitPrice * item.quantity),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                IconButton(onClick = { viewModel.remove(item) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Eliminar del carro",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val ordered = orderedId
    if (ordered != null) {
        AlertDialog(
            onDismissRequest = { orderedId = null },
            title = { Text("¡Pedido solicitado! 🎉") },
            text = {
                Text("Tu pedido #$ordered quedó en estado \"${OrderStatus.SOLICITADO}\". Es una compra simulada: no se realizó ningún cobro.")
            },
            confirmButton = {
                TextButton(onClick = { orderedId = null; onOpenOrders() }) { Text("Ver mis pedidos") }
            },
            dismissButton = {
                TextButton(onClick = { orderedId = null }) { Text("Cerrar") }
            }
        )
    }
}

/** Historial de pedidos en estado "Solicitado". */
@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    viewModel: OrdersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    AppScaffold(title = "Mis pedidos", subtitle = "Compras simuladas", onBack = onBack) { padding ->
        if (orders.isEmpty()) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    emoji = "📦",
                    title = "Aún no tienes pedidos",
                    message = "Cuando solicites un pedido desde tu carro, aparecerá aquí."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.order.id }) { orderUi ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Pedido #${orderUi.order.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        orderUi.order.status,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                formatDate(orderUi.order.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            orderUi.items.forEach { line ->
                                Row {
                                    Text(
                                        "${line.quantity} × ${line.productName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(formatClp(line.quantity * line.unitPrice), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 10.dp))
                            Row {
                                Text("Total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text(
                                    formatClp(orderUi.order.total),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
