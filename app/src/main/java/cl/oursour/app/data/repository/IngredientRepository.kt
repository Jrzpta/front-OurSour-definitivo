package cl.oursour.app.data.repository

import androidx.room.withTransaction
import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.IngredientEntity
import cl.oursour.app.data.local.IngredientWithOwned
import cl.oursour.app.data.local.UserIngredientEntity
import kotlinx.coroutines.flow.Flow

/** CU-01: ingredientes que el usuario declara tener en su hogar. */
class IngredientRepository(private val db: AppDatabase) {
    private val dao = db.ingredientDao()

    fun observe(userId: Long): Flow<List<IngredientWithOwned>> = dao.observeWithOwned(userId)

    fun observeOwnedIds(userId: Long): Flow<List<Long>> = dao.observeOwnedIds(userId)

    suspend fun getOwnedIds(userId: Long): List<Long> = dao.getOwnedIds(userId)

    suspend fun getOwnedNames(userId: Long): List<String> = dao.getOwnedNames(userId)

    /** Reemplaza la selección anterior por la nueva, todo en una sola transacción. */
    suspend fun saveSelection(userId: Long, ingredientIds: Set<Long>) {
        db.withTransaction {
            dao.clearUserIngredients(userId)
            dao.insertUserIngredients(ingredientIds.map { UserIngredientEntity(userId, it) })
        }
    }

    /** Flujo alternativo de CU-01: agregar manualmente un ingrediente que no está en el listado. */
    suspend fun addCustom(name: String, category: String): Result<Long> {
        val clean = name.trim()
        if (clean.isEmpty()) return Result.failure(Exception("Escribe el nombre del ingrediente"))
        if (clean.length > 40) return Result.failure(Exception("El nombre es demasiado largo"))
        if (dao.findByName(clean) != null) {
            return Result.failure(Exception("\"$clean\" ya existe en el listado"))
        }
        val id = dao.insert(IngredientEntity(name = clean, category = category))
        return Result.success(id)
    }
}
