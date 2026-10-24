package cl.oursour.app.data.repository

import androidx.room.withTransaction
import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.OrderEntity
import cl.oursour.app.data.local.OrderItemEntity
import cl.oursour.app.data.local.OrderStatus
import cl.oursour.app.data.local.OrderWithTotal
import cl.oursour.app.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * CU-04: carro de compras.
 * El carro es un pedido en estado "En preparación"; al solicitarlo pasa a "Solicitado".
 * La compra es simulada (no hay pasarela de pago, tal como indica el anexo).
 */
class CartRepository(private val db: AppDatabase) {
    private val orderDao = db.orderDao()
    private val productDao = db.productDao()

    companion object {
        const val MAX_QUANTITY = 10
    }

    fun observeCart(userId: Long): Flow<List<OrderItemEntity>> =
        orderDao.observeItemsByStatus(userId, OrderStatus.EN_PREPARACION)

    fun observeHistory(userId: Long): Flow<List<OrderWithTotal>> =
        orderDao.observeOrdersWithTotal(userId, OrderStatus.SOLICITADO)

    fun observeHistoryItems(userId: Long): Flow<List<OrderItemEntity>> =
        orderDao.observeItemsByStatus(userId, OrderStatus.SOLICITADO)

    /** Agrega 1 unidad del producto (o suma 1 si ya estaba en el carro). */
    suspend fun addProduct(userId: Long, product: ProductEntity): Result<Unit> {
        if (!product.active) return Result.failure(Exception("Este producto no está disponible"))
        if (product.stock <= 0) return Result.failure(Exception("Producto sin stock"))

        return db.withTransaction {
            val openOrder = orderDao.findByStatus(userId, OrderStatus.EN_PREPARACION)
            val orderId = openOrder?.id ?: orderDao.insertOrder(
                OrderEntity(userId = userId, status = OrderStatus.EN_PREPARACION, createdAt = System.currentTimeMillis())
            )
            val existing = orderDao.findItem(orderId, product.id)
            if (existing == null) {
                orderDao.insertItem(
                    OrderItemEntity(
                        orderId = orderId,
                        productId = product.id,
                        productName = product.name,
                        unitPrice = product.price,
                        quantity = 1
                    )
                )
                Result.success(Unit)
            } else if (existing.quantity >= MAX_QUANTITY) {
                Result.failure(Exception("Máximo $MAX_QUANTITY unidades por producto"))
            } else {
                orderDao.updateItem(existing.copy(quantity = existing.quantity + 1))
                Result.success(Unit)
            }
        }
    }

    /** Suma o resta unidades; si la cantidad llega a 0 el producto sale del carro. */
    suspend fun changeQuantity(item: OrderItemEntity, delta: Int) {
        val newQuantity = (item.quantity + delta).coerceAtMost(MAX_QUANTITY)
        if (newQuantity <= 0) orderDao.deleteItem(item) else orderDao.updateItem(item.copy(quantity = newQuantity))
    }

    suspend fun remove(item: OrderItemEntity) = orderDao.deleteItem(item)

    /** Pasa el carro a "Solicitado", descuenta el stock simulado y devuelve el número de pedido. */
    suspend fun checkout(userId: Long): Result<Long> = db.withTransaction {
        val order = orderDao.findByStatus(userId, OrderStatus.EN_PREPARACION)
            ?: return@withTransaction Result.failure(Exception("Tu carro está vacío"))
        val items = orderDao.getItems(order.id)
        if (items.isEmpty()) return@withTransaction Result.failure(Exception("Tu carro está vacío"))

        // Primero validamos todo el stock; solo si está OK se descuenta.
        val products = items.map { it to productDao.findById(it.productId) }
        for ((item, product) in products) {
            if (product == null || !product.active) {
                return@withTransaction Result.failure(Exception("\"${item.productName}\" ya no está disponible"))
            }
            if (product.stock < item.quantity) {
                return@withTransaction Result.failure(
                    Exception("Stock insuficiente de \"${item.productName}\" (quedan ${product.stock})")
                )
            }
        }
        for ((item, product) in products) {
            productDao.update(product!!.copy(stock = product.stock - item.quantity))
        }
        orderDao.updateStatus(order.id, OrderStatus.SOLICITADO, System.currentTimeMillis())
        Result.success(order.id)
    }
}
