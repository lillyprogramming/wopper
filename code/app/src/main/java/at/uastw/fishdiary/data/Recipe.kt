package at.uastw.fishdiary.data

data class Recipe(
    val id: Int = 0,
    val mealType: String,
    val name: String,
    val instructions: String,
    val ingredients: String,
    val totalTime: Int = 0,
    val imagePath: String? = null
)
