package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.oursour.app.OurSourApp

/** Obtiene la clase Application desde los "extras" con los que se crea un ViewModel. */
fun CreationExtras.ourSourApplication(): OurSourApp = this[APPLICATION_KEY] as OurSourApp

/**
 * Fábrica única de ViewModels. Cada "initializer" le explica a Android
 * cómo construir un ViewModel entregándole el AppContainer.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AppViewModel(ourSourApplication().container) }
        initializer { AuthViewModel(ourSourApplication().container) }
        initializer { HomeViewModel(ourSourApplication().container) }
        initializer { IngredientsViewModel(ourSourApplication().container) }
        initializer { RecipesViewModel(ourSourApplication().container) }
        initializer { RecipeDetailViewModel(this.createSavedStateHandle(), ourSourApplication().container) }
        initializer { CartViewModel(ourSourApplication().container) }
        initializer { OrdersViewModel(ourSourApplication().container) }
        initializer { ScannerViewModel(ourSourApplication().container) }
        initializer { AdminCatalogViewModel(ourSourApplication().container) }
        initializer { AdminProductFormViewModel(this.createSavedStateHandle(), ourSourApplication().container) }
        initializer { AdminMetricsViewModel(ourSourApplication().container) }
    }
}
