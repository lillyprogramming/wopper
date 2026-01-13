package at.uastw.fishdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealType: String,
    val name: String,
    val imagePath: String? = null,
    val totalTime: Int,
    val difficulty: Int,
)
