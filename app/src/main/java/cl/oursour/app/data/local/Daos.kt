package cl.oursour.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// Clases auxiliares para resultados de consultas con JOIN / GROUP BY
// ---------------------------------------------------------------------------

/** Ingrediente junto con la marca de si el usuario actual lo posee. */
data class IngredientWithOwned(
    val id: Long,
    val name: String,
    val category: String,
    val owned: Boolean
)

/** Pedido con su monto total y cantidad de unidades calculados en SQL. */
data class OrderWithTotal(
    val id: Long,
    val userId: Long,
    val status: String,
    val createdAt: Long,
    val total: Int,
    val itemCount: Int
)

/** Fila de ranking: nombre y cuántas veces apareció. */
data class TrendRow(
    val name: String,
    val total: Int
)

// ---------------------------------------------------------------------------
// DAOs (Data Access Objects): aquí vive todo el SQL de la app
// ---------------------------------------------------------------------------

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ingredient: IngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>

    @Query("SELECT * FROM ingredients ORDER BY name")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): IngredientEntity?

    @Query(
        "SELECT i.id AS id, i.name AS name, i.category AS category, " +
            "CASE WHEN ui.userId IS NULL THEN 0 ELSE 1 END AS owned " +
            "FROM ingredients i " +
            "LEFT JOIN user_ingredients ui ON ui.ingredientId = i.id AND ui.userId = :userId " +
            "ORDER BY i.name"
    )
    fun observeWithOwned(userId: Long): Flow<List<IngredientWithOwned>>

    @Query("SELECT ingredientId FROM user_ingredients WHERE userId = :userId")
    fun observeOwnedIds(userId: Long): Flow<List<Long>>

    @Query("SELECT ingredientId FROM user_ingredients WHERE userId = :userId")
    suspend fun getOwnedIds(userId: Long): List<Long>

    @Query(
        "SELECT i.name FROM ingredients i " +
            "INNER JOIN user_ingredients ui ON ui.ingredientId = i.id " +
            "WHERE ui.userId = :userId ORDER BY i.name"
    )
    suspend fun getOwnedNames(userId: Long): List<String>

    @Query("DELETE FROM user_ingredients WHERE userId = :userId")
    suspend fun clearUserIngredients(userId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserIngredients(list: List<UserIngredientEntity>)
}

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Insert
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun update(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY name")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertRefs(refs: List<RecipeIngredientEntity>)

    @Query("SELECT * FROM recipes ORDER BY title")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipe_ingredients")
    fun observeAllRefs(): Flow<List<RecipeIngredientEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun findById(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE productId = :productId LIMIT 1")
    suspend fun findByProductId(productId: Long): RecipeEntity?
}

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertItem(item: OrderItemEntity): Long

    @Update
    suspend fun updateItem(item: OrderItemEntity)

    @Delete
    suspend fun deleteItem(item: OrderItemEntity)

    @Query("UPDATE orders SET status = :status, createdAt = :createdAt WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String, createdAt: Long)

    @Query("SELECT * FROM orders WHERE userId = :userId AND status = :status ORDER BY id DESC LIMIT 1")
    suspend fun findByStatus(userId: Long, status: String): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId AND productId = :productId LIMIT 1")
    suspend fun findItem(orderId: Long, productId: Long): OrderItemEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id")
    suspend fun getItems(orderId: Long): List<OrderItemEntity>

    @Query(
        "SELECT oi.* FROM order_items oi " +
            "INNER JOIN orders o ON o.id = oi.orderId " +
            "WHERE o.userId = :userId AND o.status = :status ORDER BY oi.id"
    )
    fun observeItemsByStatus(userId: Long, status: String): Flow<List<OrderItemEntity>>

    @Query(
        "SELECT o.id AS id, o.userId AS userId, o.status AS status, o.createdAt AS createdAt, " +
            "COALESCE(SUM(i.quantity * i.unitPrice), 0) AS total, " +
            "COALESCE(SUM(i.quantity), 0) AS itemCount " +
            "FROM orders o LEFT JOIN order_items i ON i.orderId = o.id " +
            "WHERE o.userId = :userId AND o.status = :status " +
            "GROUP BY o.id ORDER BY o.createdAt DESC"
    )
    fun observeOrdersWithTotal(userId: Long, status: String): Flow<List<OrderWithTotal>>
}

@Dao
interface SearchLogDao {
    @Insert
    suspend fun insertAll(logs: List<SearchLogEntity>)

    @Query("SELECT COUNT(*) FROM search_logs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM search_logs WHERE createdAt >= :since")
    fun observeTotal(since: Long): Flow<Int>

    @Query(
        "SELECT refName AS name, COUNT(*) AS total FROM search_logs " +
            "WHERE type = :type AND createdAt >= :since " +
            "GROUP BY refName ORDER BY total DESC, refName ASC LIMIT :limit"
    )
    fun observeTop(type: String, since: Long, limit: Int): Flow<List<TrendRow>>
}
