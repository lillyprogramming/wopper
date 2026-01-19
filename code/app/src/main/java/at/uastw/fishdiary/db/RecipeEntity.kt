package at.uastw.fishdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealType: String,
    val categories: String,
    val name: String,
    val imagePath: String? = null,
    val notes: String,
    val totalTime: Int,
    val difficulty: Int,
    val servingSize: Int,
)
