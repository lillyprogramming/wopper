package at.uastw.fishdiary.data

data class Recipe(
    val id: Int = 0,
    val mealType: String,
    val categories: String,
    val name: String,
    val imagePath: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList(),
    val notes: String,
    val totalTime: Int,
    val difficulty: Int,
    val servingSize: Int,
)

