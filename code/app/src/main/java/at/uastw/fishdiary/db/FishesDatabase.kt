package at.uastw.fishdiary.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FishEntity::class],
    version = 1
)
abstract class FishesDatabase : RoomDatabase() {

    abstract fun fishesDao(): FishesDao

    companion object {
        @Volatile
        private var Instance: FishesDatabase? = null
        fun getDatabase(context: Context): FishesDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(context, FishesDatabase::class.java, "fish_database")
                    .build()
                Instance = instance
                return instance
            }
        }
    }
}