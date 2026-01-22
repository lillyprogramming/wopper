package at.uastw.wopper.data

data class Ingredient(
    val id: Int = 0,
    val recipeId: Int,
    val name: String,
    val amount: String? = null,
    val unit: String? = null
)