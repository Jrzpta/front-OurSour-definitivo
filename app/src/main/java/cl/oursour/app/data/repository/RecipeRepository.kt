package cl.oursour.app.data.repository

import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.IngredientEntity
import cl.oursour.app.data.local.LogType
import cl.oursour.app.data.local.ProductEntity
import cl.oursour.app.data.local.RecipeEntity
import cl.oursour.app.data.local.SearchLogEntity
import cl.oursour.app.util.MatchCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Una receta junto con su producto Our Sour y qué tan compatible es con el usuario. */
data class RecipeMatch(
    val recipe: RecipeEntity,
    val product: ProductEntity?,
    val required: List<IngredientEntity>,
    val missing: List<IngredientEntity>
) {
    val ownedCount: Int get() = required.size - missing.size
    val percent: Int get() = MatchCalculator.percent(ownedCount, required.size)
    val steps: List<String> get() = recipe.steps.split("\n").filter { it.isNotBlank() }
}

/** Ordena de mayor a menor compatibilidad; a igualdad, las que menos ingredientes faltan. */
fun List<RecipeMatch>.rankedByRelevance(): List<RecipeMatch> =
    sortedWith(
        compareByDescending<RecipeMatch> { it.percent }
            .thenBy { it.missing.size }
            .thenBy { it.recipe.title }
    )

/**
 * CU-02 y CU-03: recetas compatibles y detalle.
 *
 * NOTA PARA LA UNIDAD 3: hoy las recetas se leen de Room (datos locales).
 * Cuando conecten el backend Spring Boot, esta es la clase que se cambia
 * para llamar a la API con Retrofit; las pantallas y ViewModels no se tocan.
 */
class RecipeRepository(db: AppDatabase) {
    private val recipeDao = db.recipeDao()
    private val ingredientDao = db.ingredientDao()
    private val productDao = db.productDao()
    private val logDao = db.searchLogDao()

    fun observeMatches(userId: Long): Flow<List<RecipeMatch>> =
        combine(
            recipeDao.observeAll(),
            recipeDao.observeAllRefs(),
            ingredientDao.observeAll(),
            productDao.observeAll(),
            ingredientDao.observeOwnedIds(userId)
        ) { recipes, refs, ingredients, products, ownedIds ->
            val ingredientById = ingredients.associateBy { it.id }
            val productById = products.associateBy { it.id }
            val ownedSet = ownedIds.toSet()
            val refsByRecipe = refs.groupBy { it.recipeId }

            recipes.map { recipe ->
                val required = refsByRecipe[recipe.id].orEmpty()
                    .mapNotNull { ingredientById[it.ingredientId] }
                val missing = required.filter { it.id !in ownedSet }
                RecipeMatch(recipe, productById[recipe.productId], required, missing)
            }
        }

    fun observeMatch(userId: Long, recipeId: Long): Flow<RecipeMatch?> =
        observeMatches(userId).map { list -> list.firstOrNull { it.recipe.id == recipeId } }

    suspend fun getRecipe(recipeId: Long): RecipeEntity? = recipeDao.findById(recipeId)

    suspend fun findRecipeIdByProduct(productId: Long): Long? =
        recipeDao.findByProductId(productId)?.id

    /** Guarda en el historial cada ingrediente usado en una búsqueda (alimenta CU-06). */
    suspend fun logSearch(ingredientNames: List<String>) {
        val now = System.currentTimeMillis()
        logDao.insertAll(
            ingredientNames.map { SearchLogEntity(type = LogType.INGREDIENT, refName = it, createdAt = now) }
        )
    }

    /** Guarda en el historial que se consultó una receta (alimenta CU-06). */
    suspend fun logRecipeView(title: String) {
        logDao.insertAll(
            listOf(SearchLogEntity(type = LogType.RECIPE, refName = title, createdAt = System.currentTimeMillis()))
        )
    }
}
