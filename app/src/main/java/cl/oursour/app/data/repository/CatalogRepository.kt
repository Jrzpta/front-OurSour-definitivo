package cl.oursour.app.data.repository

import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

/** CU-05: el administrador gestiona el catálogo de productos. */
class CatalogRepository(db: AppDatabase) {
    private val dao = db.productDao()

    fun observeAll(): Flow<List<ProductEntity>> = dao.observeAll()

    suspend fun findById(id: Long): ProductEntity? = dao.findById(id)

    /** Crea el producto si su id es 0; si no, lo actualiza. */
    suspend fun save(product: ProductEntity) {
        if (product.id == 0L) dao.insert(product) else dao.update(product)
    }

    suspend fun setActive(product: ProductEntity, active: Boolean) {
        dao.update(product.copy(active = active))
    }
}
