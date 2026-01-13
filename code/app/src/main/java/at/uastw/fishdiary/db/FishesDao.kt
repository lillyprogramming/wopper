package at.uastw.fishdiary.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FishesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFish(fishEntity: FishEntity)

    @Update
    suspend fun updateFish(fishEntity: FishEntity)

    @Delete
    suspend fun deleteFish(fishEntity: FishEntity)

    @Query("SELECT * FROM Bob WHERE id = :id")
    suspend fun findFishById(id: Int): FishEntity

    @Query("SELECT * FROM Bob")
    fun getAllFishes(): Flow<List<FishEntity>>
}
