package cl.oursour.app.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.oursour.app.data.session.Session
import cl.oursour.app.ui.components.AppHeader
import cl.oursour.app.ui.components.BrandBackground
import cl.oursour.app.ui.theme.Amber
import cl.oursour.app.ui.theme.Ink
import cl.oursour.app.ui.theme.Lime
import cl.oursour.app.viewmodel.AppViewModel
import cl.oursour.app.viewmodel.AppViewModelProvider
import cl.oursour.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/** Pantalla de bienvenida: espera a que la base de datos esté lista y decide a dónde ir. */
@Composable
fun SplashScreen(
    appViewModel: AppViewModel,
    onFinished: (Session?) -> Unit
) {
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(durationMillis = 900),
        label = "logoScale"
    )

    LaunchedEffect(Unit) {
        started = true
        appViewModel.awaitSeed()
        delay(1_200)
        onFinished(appViewModel.session.value)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Lime, Amber))),
                contentAlignment = Alignment.Center
            ) {
                Text("🍋", fontSize = 56.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Our Sour",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Tu bar en casa",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        }
    }
}

/** Inicio de sesión (consumidor o administrador, según el usuario). */
@Composable
fun LoginScreen(
    onLoggedIn: (Session) -> Unit,
    onRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Lime, Amber))),
            contentAlignment = Alignment.Center
        ) {
            Text("🍋", fontSize = 40.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Bienvenido a Our Sour", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            "Descubre qué cócteles puedes preparar con lo que ya tienes en casa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = { Text("Correo") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(
                state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.login(email, password, onLoggedIn) },
            enabled = !state.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Ink)
            } else {
                Text("Iniciar sesión", fontWeight = FontWeight.Bold)
            }
        }
        TextButton(onClick = onRegister) { Text("¿No tienes cuenta? Regístrate") }

        Spacer(Modifier.height(20.dp))
        Text(
            "Cuentas de demostración (datos ficticios)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { email = "cliente@oursour.cl"; password = "cliente123" },
                label = { Text("Cliente demo") }
            )
            AssistChip(
                onClick = { email = "admin@oursour.cl"; password = "admin123" },
                label = { Text("Admin demo") }
            )
        }
    }
}

/** Registro de un nuevo consumidor final. */
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: (Session) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        AppHeader(title = "Crear cuenta", subtitle = "Solo te tomará un minuto", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; viewModel.clearError() },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = { Text("Contraseña (mínimo 6 caracteres)") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it; viewModel.clearError() },
                label = { Text("Repite la contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(
                    state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.register(name, email, password, confirm, onRegistered) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Ink)
                } else {
                    Text("Crear cuenta", fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Tus datos se guardan solo en este teléfono. Usa información de prueba.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
