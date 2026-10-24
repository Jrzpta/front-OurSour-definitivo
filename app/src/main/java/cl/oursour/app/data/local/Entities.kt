package cl.oursour.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ---------------------------------------------------------------------------
// Constantes de dominio (evitan "strings mágicos" repartidos por el código)
// ---------------------------------------------------------------------------

object Roles {
    const val CONSUMER = "CONSUMER"
    const val ADMIN = "ADMIN"
}

object Categories {
    const val DESTILADO = "DESTILADO"
    const val LICOR = "LICOR"
    const val JUGO = "JUGO"
    const val FRUTA = "FRUTA"

    val all = listOf(DESTILADO, LICOR, JUGO, FRUTA)

    fun label(category: String): String = when (category) {
        DESTILADO -> "Destilados"
        LICOR -> "Licores"
        JUGO -> "Jugos y bebidas"
        FRUTA -> "Frutas y hierbas"
        else -> category
    }

    fun emoji(category: String): String = when (category) {
        DESTILADO -> "🥃"
        LICOR -> "🍸"
        JUGO -> "🧃"
        FRUTA -> "🍋"
        else -> "🍹"
    }
}

object OrderStatus {
    const val EN_PREPARACION = "En preparación"
    const val SOLICITADO = "Solicitado"
}

object LogType {
    const val INGREDIENT = "INGREDIENT"
    const val RECIPE = "RECIPE"
}

// ---------------------------------------------------------------------------
// Entidades (cada una es una tabla de SQLite)
// ---------------------------------------------------------------------------

/** Usuario local de la app (consumidor o administrador). */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String
)

/** Ingrediente: Identificador, nombre del insumo y categoría (anexo 3.2). */
@Entity(tableName = "ingredients", indices = [Index(value = ["name"], unique = true)])
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String
)

/** Relación usuario <-> ingredientes que declaró tener (CU-01). */
@Entity(
    tableName = "user_ingredients",
    primaryKeys = ["userId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientId")]
)
data class UserIngredientEntity(
    val userId: Long,
    val ingredientId: Long
)

/** Producto del catálogo: id, nombre comercial, descripción, precio y stock simulado. */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val active: Boolean = true,
    val emoji: String = "🍹"
)

/** Receta: id, título, pasos de preparación y producto Our Sour asociado. */
@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"]
        )
    ],
    indices = [Index("productId")]
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    /** Pasos separados por salto de línea ("\n"). */
    val steps: String,
    val productId: Long,
    val alcoholic: Boolean,
    val minutes: Int,
    val emoji: String
)

/** Ingredientes requeridos por cada receta (relación muchos a muchos). */
@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientId")]
)
data class RecipeIngredientEntity(
    val recipeId: Long,
    val ingredientId: Long
)

/** Carro/Pedido: id, estado ("En preparación" o "Solicitado") y fecha. */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val status: String,
    val createdAt: Long
)

/** Línea de un pedido: producto, cantidad y precio unitario congelado al agregarlo. */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId"), Index(value = ["orderId", "productId"], unique = true)]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Int,
    val quantity: Int
)

/** Registro de búsquedas/consultas que alimenta las métricas del administrador (CU-06). */
@Entity(tableName = "search_logs")
data class SearchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val refName: String,
    val createdAt: Long
)
