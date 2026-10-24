package cl.oursour.app

import android.content.Context
import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.SeedData
import cl.oursour.app.data.repository.AuthRepository
import cl.oursour.app.data.repository.CartRepository
import cl.oursour.app.data.repository.CatalogRepository
import cl.oursour.app.data.repository.IngredientRepository
import cl.oursour.app.data.repository.MetricsRepository
import cl.oursour.app.data.repository.RecipeRepository
import cl.oursour.app.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor de dependencias manual (inyección de dependencias "casera").
 * Crea una sola vez la base de datos, la sesión y los repositorios,
 * y los entrega a los ViewModels.
 */
class AppContainer(context: Context) {

    val database: AppDatabase = AppDatabase.get(context)
    val session = SessionManager(context)

    val authRepository = AuthRepository(database, session)
    val ingredientRepository = IngredientRepository(database)
    val recipeRepository = RecipeRepository(database)
    val catalogRepository = CatalogRepository(database)
    val cartRepository = CartRepository(database)
    val metricsRepository = MetricsRepository(database)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Carga los datos de prueba la primera vez; la pantalla Splash espera a que termine. */
    val seedJob: Job = appScope.launch { SeedData.seedIfEmpty(database) }
}
