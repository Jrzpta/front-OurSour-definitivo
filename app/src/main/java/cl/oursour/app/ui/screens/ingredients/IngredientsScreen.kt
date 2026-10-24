package cl.oursour.app.ui.screens.ingredients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.data.local.Categories
import cl.oursour.app.ui.components.AppScaffold
import cl.oursour.app.ui.components.EmptyState
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.IngredientsViewModel

/** CU-01: Declarar ingredientes disponibles. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientsScreen(
    onSeeRecipes: () -> Unit,
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // Muestra los mensajes del ViewModel (confirmación de guardado, errores, etc.)
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val result = snackbar.showSnackbar(message.text, actionLabel = message.actionLabel)
            if (result == SnackbarResult.ActionPerformed) onSeeRecipes()
        }
    }

    AppScaffold(
        title = "Mis ingredientes",
        subtitle = "${state.selected.size} seleccionados",
        snackbarHostState = snackbar,
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar ingrediente manualmente")
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = viewModel::save,
                    enabled = state.hasChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(
                        text = if (state.hasChanges) "  Guardar mis ingredientes" else "  Todo guardado",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Buscar ingrediente…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.groups.isEmpty()) {
                EmptyState(
                    emoji = "🔍",
                    title = "Sin resultados",
                    message = "No encontramos ese ingrediente. Puedes agregarlo manualmente con el botón +.",
                    actionLabel = "Agregar ingrediente",
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(state.groups, key = { it.first }) { group ->
                        val category = group.first
                        val categoryItems = group.second
                        Column {
                            Text(
                                text = "${Categories.emoji(category)}  ${Categories.label(category)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                categoryItems.forEach { ingredient ->
                                    val isSelected = ingredient.id in state.selected
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggle(ingredient.id) },
                                        label = { Text(ingredient.name) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Filled.Check, contentDescription = null) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category ->
                viewModel.addCustom(name, category)
                showAddDialog = false
            }
        )
    }
}

/** Flujo alternativo de CU-01: agregar un ingrediente indicando nombre y categoría. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Categories.DESTILADO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar ingrediente") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Categoría", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Categories.all.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(Categories.label(c)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, category) }) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
