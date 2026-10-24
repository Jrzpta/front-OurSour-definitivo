package cl.oursour.app.ui.navigation

/** Rutas (direcciones) de cada pantalla de la app. */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Consumidor final
    const val HOME = "home"
    const val INGREDIENTS = "ingredients"
    const val RECIPES = "recipes"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val ORDERS = "orders"
    const val SCANNER = "scanner"
    const val RECIPE_DETAIL = "recipe/{recipeId}"
    fun recipeDetail(id: Long) = "recipe/$id"

    // Administrador
    const val ADMIN_CATALOG = "admin_catalog"
    const val ADMIN_METRICS = "admin_metrics"
    const val ADMIN_PROFILE = "admin_profile"
    const val ADMIN_FORM = "admin_product/{productId}"
    fun adminForm(id: Long) = "admin_product/$id"
}
