package at.uastw.fishdiary.data

data class Fish(
    val id: Int = 0,
    val name: String,
    val description: String,
    val categories: String,
    val hasSeen: Boolean,
    val imagePath: String? = null
)
