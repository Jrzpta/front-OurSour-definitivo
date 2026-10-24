package cl.oursour.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.oursour.app.ui.screens.admin.AdminCatalogScreen
import cl.oursour.app.ui.screens.admin.AdminMetricsScreen
import cl.oursour.app.ui.screens.admin.AdminProductFormScreen
import cl.oursour.app.ui.screens.auth.LoginScreen
import cl.oursour.app.ui.screens.auth.RegisterScreen
import cl.oursour.app.ui.screens.auth.SplashScreen
import cl.oursour.app.ui.screens.cart.CartScreen
import cl.oursour.app.ui.screens.cart.OrdersScreen
import cl.oursour.app.ui.screens.home.HomeScreen
import cl.oursour.app.ui.screens.ingredients.IngredientsScreen
import cl.oursour.app.ui.screens.profile.ProfileScreen
import cl.oursour.app.ui.screens.recipes.RecipeDetailScreen
import cl.oursour.app.ui.screens.recipes.RecipesScreen
import cl.oursour.app.ui.screens.scanner.ScannerScreen
import cl.oursour.app.viewmodel.AppViewModel
import cl.oursour.app.viewmodel.AppViewModelProvider

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val consumerTabs = listOf(
    TabItem(Routes.HOME, "Inicio", Icons.Filled.Home),
    TabItem(Routes.INGREDIENTS, "Ingredientes", Icons.AutoMirrored.Filled.List),
    TabItem(Routes.RECIPES, "Recetas", Icons.Filled.Search),
    TabItem(Routes.CART, "Carro", Icons.Filled.ShoppingCart),
    TabItem(Routes.PROFILE, "Perfil", Icons.Filled.Person)
)

private val adminTabs = listOf(
    TabItem(Routes.ADMIN_CATALOG, "Catálogo", Icons.AutoMirrored.Filled.List),
    TabItem(Routes.ADMIN_METRICS, "Métricas", Icons.Filled.Star),
    TabItem(Routes.ADMIN_PROFILE, "Perfil", Icons.Filled.Person)
)

/**
 * Raíz de la app: arma la barra de navegación inferior y el NavHost,
 * que decide qué pantalla mostrar según la ruta actual.
 */
@Composable
fun OurSourNavHost(appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val session by appViewModel.session.collectAsStateWithLifecycle()
    val cartCount by appViewModel.cartCount.collectAsStateWithLifecycle()

    val isAdmin = session?.isAdmin == true
    val tabs = if (isAdmin) adminTabs else consumerTabs
    val showBottomBar = currentRoute != null && tabs.any { it.route == currentRoute }
    val homeRoute = if (isAdmin) Routes.ADMIN_CATALOG else Routes.HOME

    // Cambia de pestaña dejando siempre "Inicio" (o "Catálogo") como base del historial.
    // No usamos saveState/restoreState a propósito: así los ViewModels de un usuario
    // nunca se reutilizan si luego inicia sesión otra persona.
    fun goToTab(route: String) {
        navController.navigate(route) {
            popUpTo(homeRoute)
            launchSingleTop = true
        }
    }

    fun logout() {
        appViewModel.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { goToTab(tab.route) },
                            icon = {
                                if (tab.route == Routes.CART && cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            // Solo aplicamos el espacio inferior; cada pantalla maneja su parte superior.
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ---------------- Acceso ----------------
            composable(Routes.SPLASH) {
                SplashScreen(
                    appViewModel = appViewModel,
                    onFinished = { s ->
                        val destination = when {
                            s == null -> Routes.LOGIN
                            s.isAdmin -> Routes.ADMIN_CATALOG
                            else -> Routes.HOME
                        }
                        navController.navigate(destination) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { s -> navigateAfterAuth(navController, s.isAdmin, Routes.LOGIN) },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegistered = { s -> navigateAfterAuth(navController, s.isAdmin, Routes.LOGIN) }
                )
            }

            // ---------------- Consumidor final ----------------
            composable(Routes.HOME) {
                HomeScreen(
                    onSeeRecipes = { goToTab(Routes.RECIPES) },
                    onDeclareIngredients = { goToTab(Routes.INGREDIENTS) },
                    onScanQr = { navController.navigate(Routes.SCANNER) },
                    onOpenRecipe = { id -> navController.navigate(Routes.recipeDetail(id)) }
                )
            }
            composable(Routes.INGREDIENTS) {
                IngredientsScreen(onSeeRecipes = { goToTab(Routes.RECIPES) })
            }
            composable(Routes.RECIPES) {
                RecipesScreen(
                    onOpenRecipe = { id -> navController.navigate(Routes.recipeDetail(id)) },
                    onDeclareIngredients = { goToTab(Routes.INGREDIENTS) },
                    onScanQr = { navController.navigate(Routes.SCANNER) }
                )
            }
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) {
                RecipeDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCart = { goToTab(Routes.CART) }
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onExploreRecipes = { goToTab(Routes.RECIPES) },
                    onOpenOrders = { navController.navigate(Routes.ORDERS) }
                )
            }
            composable(Routes.ORDERS) {
                OrdersScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SCANNER) {
                ScannerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenRecipe = { id ->
                        navController.navigate(Routes.recipeDetail(id)) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = { logout() },
                    onOpenOrders = { navController.navigate(Routes.ORDERS) },
                    onScanQr = { navController.navigate(Routes.SCANNER) }
                )
            }

            // ---------------- Administrador ----------------
            composable(Routes.ADMIN_CATALOG) {
                AdminCatalogScreen(
                    onNewProduct = { navController.navigate(Routes.adminForm(-1L)) },
                    onEditProduct = { id -> navController.navigate(Routes.adminForm(id)) }
                )
            }
            composable(
                route = Routes.ADMIN_FORM,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) {
                AdminProductFormScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADMIN_METRICS) {
                AdminMetricsScreen()
            }
            composable(Routes.ADMIN_PROFILE) {
                ProfileScreen(
                    onLogout = { logout() },
                    onOpenOrders = {},
                    onScanQr = {}
                )
            }
        }
    }
}

/** Después de iniciar sesión o registrarse: ir al inicio que corresponda y borrar el login del historial. */
private fun navigateAfterAuth(navController: NavHostController, isAdmin: Boolean, popTo: String) {
    val destination = if (isAdmin) Routes.ADMIN_CATALOG else Routes.HOME
    navController.navigate(destination) {
        popUpTo(popTo) { inclusive = true }
    }
}
