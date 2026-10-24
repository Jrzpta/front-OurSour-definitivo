package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.util.QrParser
import cl.oursour.app.util.QrTarget
import kotlinx.coroutines.launch

sealed interface ScanResult {
    data class OpenRecipe(val id: Long) : ScanResult
    data class Error(val message: String) : ScanResult
}

/** Lectura de QR: traduce el texto del código a una receta de la base local. */
class ScannerViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.recipeRepository

    fun resolve(raw: String, onResult: (ScanResult) -> Unit) {
        viewModelScope.launch {
            val result: ScanResult = when (val target = QrParser.parse(raw)) {
                is QrTarget.Recipe ->
                    if (repo.getRecipe(target.id) != null) ScanResult.OpenRecipe(target.id)
                    else ScanResult.Error("La receta ${target.id} no existe en el catálogo")

                is QrTarget.Product -> {
                    val recipeId = repo.findRecipeIdByProduct(target.id)
                    if (recipeId != null) ScanResult.OpenRecipe(recipeId)
                    else ScanResult.Error("Ese producto aún no tiene una receta asociada")
                }

                QrTarget.Invalid -> ScanResult.Error("Este código QR no pertenece a Our Sour")
            }
            onResult(result)
        }
    }
}
