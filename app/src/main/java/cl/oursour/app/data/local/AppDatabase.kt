package cl.oursour.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local (SQLite mediante Room).
 * Si agregas o cambias una entidad, sube el número de "version".
 */
@Database(
    entities = [
        UserEntity::class,
        IngredientEntity::class,
        UserIngredientEntity::class,
        ProductEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SearchLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun productDao(): ProductDao
    abstract fun recipeDao(): RecipeDao
    abstract fun orderDao(): OrderDao
    abstract fun searchLogDao(): SearchLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** Patrón Singleton: una sola instancia de la base para toda la app. */
        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oursour.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
