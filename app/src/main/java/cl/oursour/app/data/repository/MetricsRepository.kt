package cl.oursour.app.data.repository

import cl.oursour.app.data.local.AppDatabase
import cl.oursour.app.data.local.LogType
import cl.oursour.app.data.local.TrendRow
import kotlinx.coroutines.flow.Flow

/** CU-06: métricas agregadas de búsquedas y tendencias para el administrador. */
class MetricsRepository(db: AppDatabase) {
    private val dao = db.searchLogDao()

    fun observeTotal(since: Long): Flow<Int> = dao.observeTotal(since)

    fun observeTopIngredients(since: Long, limit: Int = 6): Flow<List<TrendRow>> =
        dao.observeTop(LogType.INGREDIENT, since, limit)

    fun observeTopRecipes(since: Long, limit: Int = 6): Flow<List<TrendRow>> =
        dao.observeTop(LogType.RECIPE, since, limit)
}
