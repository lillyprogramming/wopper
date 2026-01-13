package at.uastw.fishdiary.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fishes")
data class FishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val categories: String,
    val hasSeen: Boolean,
    val imagePath: String? = null
)
