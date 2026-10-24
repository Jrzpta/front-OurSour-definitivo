package cl.oursour.app.data.local

import androidx.room.withTransaction
import cl.oursour.app.util.Hasher

/**
 * Datos de prueba SINTÉTICOS (el anexo prohíbe usar datos reales de Our Sour).
 * Se cargan una sola vez, la primera vez que se abre la app.
 *
 * IMPORTANTE: los ids se asignan en el orden en que aparecen estas listas
 * (1, 2, 3...). Los QR de prueba usan esos ids.
 */
object SeedData {

    private class SeedProduct(
        val name: String,
        val description: String,
        val price: Int,
        val stock: Int,
        val emoji: String
    )

    private class SeedRecipe(
        val title: String,
        val emoji: String,
        val alcoholic: Boolean,
        val minutes: Int,
        val product: String,
        val ingredients: List<String>,
        val description: String,
        val steps: List<String>
    )

    private val ingredients = listOf(
        // Destilados
        IngredientEntity(name = "Pisco", category = Categories.DESTILADO),
        IngredientEntity(name = "Ron blanco", category = Categories.DESTILADO),
        IngredientEntity(name = "Vodka", category = Categories.DESTILADO),
        IngredientEntity(name = "Gin", category = Categories.DESTILADO),
        IngredientEntity(name = "Whisky", category = Categories.DESTILADO),
        IngredientEntity(name = "Tequila", category = Categories.DESTILADO),
        // Licores
        IngredientEntity(name = "Triple sec", category = Categories.LICOR),
        IngredientEntity(name = "Amaretto", category = Categories.LICOR),
        IngredientEntity(name = "Aperol", category = Categories.LICOR),
        IngredientEntity(name = "Licor de café", category = Categories.LICOR),
        // Jugos y bebidas
        IngredientEntity(name = "Agua con gas", category = Categories.JUGO),
        IngredientEntity(name = "Ginger ale", category = Categories.JUGO),
        IngredientEntity(name = "Jugo de arándano", category = Categories.JUGO),
        IngredientEntity(name = "Jugo de piña", category = Categories.JUGO),
        IngredientEntity(name = "Jugo de maracuyá", category = Categories.JUGO),
        IngredientEntity(name = "Jugo de naranja", category = Categories.JUGO),
        // Frutas y hierbas
        IngredientEntity(name = "Limón", category = Categories.FRUTA),
        IngredientEntity(name = "Naranja", category = Categories.FRUTA),
        IngredientEntity(name = "Menta", category = Categories.FRUTA),
        IngredientEntity(name = "Frutilla", category = Categories.FRUTA),
        IngredientEntity(name = "Pepino", category = Categories.FRUTA),
        IngredientEntity(name = "Jengibre", category = Categories.FRUTA)
    )

    private val products = listOf(
        SeedProduct("Our Sour Clásico", "Mix de limón equilibrado, la base perfecta para tus sours.", 6990, 40, "🍋"),
        SeedProduct("Our Sour Maracuyá", "Mix tropical de maracuyá con notas ácidas y frescas.", 7490, 25, "🍹"),
        SeedProduct("Our Sour Frutos Rojos", "Mix de frutos rojos, dulce y ácido a la vez.", 7490, 30, "🍓"),
        SeedProduct("Our Sour Jengibre & Limón", "Mix picante de jengibre con limón para mules y fizz.", 7290, 35, "🥂"),
        SeedProduct("Our Sour Pepino & Menta", "Mix refrescante de pepino y menta, ideal para el verano.", 6990, 20, "🥒"),
        SeedProduct("Our Sour Cítrico 0%", "Base cítrica sin alcohol para mocktails listos en minutos.", 6490, 50, "🍃"),
        SeedProduct("Our Sour Piña Tropical", "Mix de piña y limón para cócteles playeros.", 7490, 18, "🍍"),
        SeedProduct("Our Sour Naranja Cítrica", "Mix de naranja y limón, suave y aromático.", 7290, 28, "🍊")
    )

    private val recipes = listOf(
        SeedRecipe(
            "Pisco Sour Clásico", "🍸", true, 5, "Our Sour Clásico", listOf("Pisco"),
            "El trago bandera de Chile, ahora en versión rápida: tu pisco y el mix Our Sour.",
            listOf(
                "Enfría una copa con hielo mientras preparas la mezcla.",
                "En una coctelera con hielo agrega una medida de pisco y dos de Our Sour Clásico.",
                "Agita con energía durante 15 segundos.",
                "Cuela en la copa y decora con una rodaja de limón."
            )
        ),
        SeedRecipe(
            "Pisco Maracuyá Sour", "🍹", true, 5, "Our Sour Maracuyá", listOf("Pisco", "Jugo de maracuyá"),
            "Un sour tropical que mezcla pisco con maracuyá fresco.",
            listOf(
                "Llena la coctelera con hielo.",
                "Agrega pisco, jugo de maracuyá y Our Sour Maracuyá.",
                "Agita fuerte y cuela sobre un vaso con hielo nuevo."
            )
        ),
        SeedRecipe(
            "Whisky Sour", "🥃", true, 5, "Our Sour Naranja Cítrica", listOf("Whisky", "Naranja"),
            "Clásico de bar con un toque cítrico de naranja.",
            listOf(
                "Agrega hielo, whisky y Our Sour Naranja Cítrica a la coctelera.",
                "Agita durante 15 segundos.",
                "Sirve en un vaso corto con hielo y decora con una cáscara de naranja."
            )
        ),
        SeedRecipe(
            "Margarita Sour", "🍹", true, 6, "Our Sour Clásico", listOf("Tequila", "Triple sec"),
            "Tequila, triple sec y el mix Our Sour para una margarita ágil.",
            listOf(
                "Escarcha el borde del vaso con sal si lo deseas.",
                "Mezcla tequila, triple sec y Our Sour Clásico con hielo.",
                "Agita y sirve con hielo fresco."
            )
        ),
        SeedRecipe(
            "Gin Sour Cítrico", "🍸", true, 5, "Our Sour Clásico", listOf("Gin", "Limón"),
            "Gin aromático con la frescura del limón.",
            listOf(
                "Exprime medio limón en la coctelera.",
                "Agrega gin, Our Sour Clásico y hielo.",
                "Agita y cuela en una copa fría."
            )
        ),
        SeedRecipe(
            "Mojito Sour", "🍃", true, 7, "Our Sour Pepino & Menta", listOf("Ron blanco", "Menta"),
            "Un mojito más fácil gracias al mix de pepino y menta.",
            listOf(
                "Machaca suavemente unas hojas de menta en el vaso.",
                "Agrega ron blanco, Our Sour Pepino & Menta y hielo.",
                "Revuelve y decora con una ramita de menta."
            )
        ),
        SeedRecipe(
            "Vodka Frutilla Sour", "🍓", true, 6, "Our Sour Frutos Rojos", listOf("Vodka", "Frutilla"),
            "Vodka con frutillas frescas y el mix de frutos rojos.",
            listOf(
                "Machaca dos o tres frutillas en la coctelera.",
                "Agrega vodka, Our Sour Frutos Rojos y hielo.",
                "Agita y cuela doble sobre hielo."
            )
        ),
        SeedRecipe(
            "Moscow Mule Sour", "🥂", true, 4, "Our Sour Jengibre & Limón", listOf("Vodka", "Ginger ale"),
            "Picante, burbujeante y muy refrescante.",
            listOf(
                "Llena un vaso alto con hielo.",
                "Agrega vodka y Our Sour Jengibre & Limón.",
                "Completa con ginger ale y revuelve suavemente."
            )
        ),
        SeedRecipe(
            "Amaretto Sour", "🍸", true, 5, "Our Sour Naranja Cítrica", listOf("Amaretto"),
            "Dulce de almendras equilibrado por la acidez del mix.",
            listOf(
                "Agrega amaretto, Our Sour Naranja Cítrica y hielo a la coctelera.",
                "Agita durante 15 segundos.",
                "Sirve en vaso corto con hielo."
            )
        ),
        SeedRecipe(
            "Cosmopolitan Sour", "🍹", true, 6, "Our Sour Frutos Rojos", listOf("Vodka", "Triple sec", "Jugo de arándano"),
            "Elegante, rosado y con el toque ácido de Our Sour.",
            listOf(
                "Enfría una copa de martini.",
                "Agita vodka, triple sec, jugo de arándano y Our Sour Frutos Rojos con hielo.",
                "Cuela en la copa fría."
            )
        ),
        SeedRecipe(
            "Spritz Sour", "🍊", true, 4, "Our Sour Naranja Cítrica", listOf("Aperol", "Agua con gas"),
            "Un spritz cítrico, liviano y perfecto para compartir.",
            listOf(
                "Llena una copa grande con hielo.",
                "Agrega Aperol y Our Sour Naranja Cítrica.",
                "Completa con agua con gas y decora con naranja."
            )
        ),
        SeedRecipe(
            "Piña Sour Tropical", "🍍", true, 6, "Our Sour Piña Tropical", listOf("Ron blanco", "Jugo de piña"),
            "Ron blanco y piña, directo a la playa.",
            listOf(
                "Agrega ron blanco, jugo de piña y Our Sour Piña Tropical a la coctelera con hielo.",
                "Agita bien.",
                "Sirve en vaso alto con hielo."
            )
        ),
        SeedRecipe(
            "Mocktail Cítrico", "🍋", false, 4, "Our Sour Cítrico 0%", listOf("Agua con gas", "Limón", "Menta"),
            "Fresco y sin alcohol, listo en minutos.",
            listOf(
                "Exprime medio limón en un vaso alto.",
                "Agrega hielo, Our Sour Cítrico 0% y unas hojas de menta.",
                "Completa con agua con gas y revuelve."
            )
        ),
        SeedRecipe(
            "Maracuyá Fizz", "🍹", false, 4, "Our Sour Maracuyá", listOf("Jugo de maracuyá", "Agua con gas"),
            "Burbujas y maracuyá, sin alcohol.",
            listOf(
                "Pon hielo en un vaso largo.",
                "Agrega jugo de maracuyá y Our Sour Maracuyá.",
                "Completa con agua con gas."
            )
        ),
        SeedRecipe(
            "Pepino Fresh", "🥒", false, 5, "Our Sour Pepino & Menta", listOf("Pepino", "Menta", "Agua con gas"),
            "El mocktail más fresco del menú.",
            listOf(
                "Corta unas rodajas de pepino y colócalas en el vaso.",
                "Agrega menta, hielo y Our Sour Pepino & Menta.",
                "Completa con agua con gas."
            )
        ),
        SeedRecipe(
            "Berry Sour 0%", "🍓", false, 5, "Our Sour Frutos Rojos", listOf("Frutilla", "Jugo de arándano"),
            "Frutos rojos, ácido y dulce, sin una gota de alcohol.",
            listOf(
                "Machaca unas frutillas en la coctelera.",
                "Agrega jugo de arándano, Our Sour Frutos Rojos y hielo.",
                "Agita y sirve."
            )
        ),
        SeedRecipe(
            "Jengibre Fizz 0%", "🥂", false, 4, "Our Sour Jengibre & Limón", listOf("Jengibre", "Ginger ale"),
            "Picante y burbujeante, un mule sin alcohol.",
            listOf(
                "Ralla un poco de jengibre fresco en un vaso con hielo.",
                "Agrega Our Sour Jengibre & Limón.",
                "Completa con ginger ale."
            )
        )
    )

    // Búsquedas sintéticas para que el panel de métricas tenga datos desde el inicio.
    private val seedIngredientSearches = listOf(
        "Pisco" to 14, "Vodka" to 11, "Limón" to 10, "Agua con gas" to 9, "Ron blanco" to 7,
        "Gin" to 6, "Tequila" to 5, "Menta" to 5, "Whisky" to 4, "Jengibre" to 3
    )
    private val seedRecipeSearches = listOf(
        "Pisco Sour Clásico" to 12, "Mocktail Cítrico" to 9, "Moscow Mule Sour" to 8,
        "Margarita Sour" to 7, "Maracuyá Fizz" to 6, "Pepino Fresh" to 5, "Spritz Sour" to 4
    )

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.productDao().count() > 0) return

        db.withTransaction {
            // 1) Usuarios de demostración (datos ficticios)
            db.userDao().insert(
                UserEntity(
                    name = "Cliente Demo",
                    email = "cliente@oursour.cl",
                    passwordHash = Hasher.sha256("cliente123"),
                    role = Roles.CONSUMER
                )
            )
            db.userDao().insert(
                UserEntity(
                    name = "Admin Our Sour",
                    email = "admin@oursour.cl",
                    passwordHash = Hasher.sha256("admin123"),
                    role = Roles.ADMIN
                )
            )

            // 2) Ingredientes: guardamos nombre -> id para armar las recetas
            val ingredientIds = db.ingredientDao().insertAll(ingredients)
            val ingredientByName = ingredients.map { it.name }.zip(ingredientIds).toMap()

            // 3) Productos del catálogo
            val productEntities = products.map {
                ProductEntity(
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    stock = it.stock,
                    active = true,
                    emoji = it.emoji
                )
            }
            val productIds = db.productDao().insertAll(productEntities)
            val productByName = products.map { it.name }.zip(productIds).toMap()

            // 4) Recetas y sus ingredientes requeridos
            recipes.forEach { r ->
                val recipeId = db.recipeDao().insert(
                    RecipeEntity(
                        title = r.title,
                        description = r.description,
                        steps = r.steps.joinToString("\n"),
                        productId = productByName.getValue(r.product),
                        alcoholic = r.alcoholic,
                        minutes = r.minutes,
                        emoji = r.emoji
                    )
                )
                db.recipeDao().insertRefs(
                    r.ingredients.map { RecipeIngredientEntity(recipeId, ingredientByName.getValue(it)) }
                )
            }

            // 5) Búsquedas sintéticas repartidas en los últimos ~30 días
            val now = System.currentTimeMillis()
            val hour = 3_600_000L
            val logs = mutableListOf<SearchLogEntity>()
            var step = 0L
            seedIngredientSearches.forEach { (name, times) ->
                repeat(times) {
                    logs += SearchLogEntity(type = LogType.INGREDIENT, refName = name, createdAt = now - (step * 5 * hour))
                    step++
                }
            }
            step = 0L
            seedRecipeSearches.forEach { (name, times) ->
                repeat(times) {
                    logs += SearchLogEntity(type = LogType.RECIPE, refName = name, createdAt = now - (step * 9 * hour))
                    step++
                }
            }
            db.searchLogDao().insertAll(logs)
        }
    }
}
