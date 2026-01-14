package at.uastw.fishdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Bob")
data class FishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val mealType: String,
    val description: String,
    val categories: String,
    val hasSeen: Boolean,
    val imagePath: String? = null
)
