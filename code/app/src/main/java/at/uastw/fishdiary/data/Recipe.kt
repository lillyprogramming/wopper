package at.uastw.fishdiary.data

data class Recipe(
    val id: Int = 0,
    val mealType: String,
    val name: String,
    val imagePath: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),
    val totalTime: Int,
    val difficulty: Int,
)

data class Ingredient(
    val id: Int = 0,
    val recipeId: Int,
    val name: String,
    val amount: String? = null,
    val unit: String? = null
)

data class Instruction(
    val id: Int = 0,
    val recipeId: Int,
    val stepNumber: Int,
    val text: String,
    val timer: Int = 0,
)
