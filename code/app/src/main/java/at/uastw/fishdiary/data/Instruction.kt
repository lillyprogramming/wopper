package at.uastw.fishdiary.data

data class Instruction(
    val id: Int = 0,
    val recipeId: Int,
    val stepNumber: Int,
    val text: String,
    val timer: Int = 0,
)
