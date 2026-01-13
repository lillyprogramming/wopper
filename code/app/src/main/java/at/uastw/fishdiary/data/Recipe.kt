package at.uastw.fishdiary.data

data class Recipe(
    val id: Int = 0,
    val mealtype: String,
    val name: String,
    val instuction: String,
    val ingrediens: String,
    val time: Boolean,
    val imagePath: String? = null
)
