package at.uastw.fishdiary.data

import at.uastw.fishdiary.db.FishEntity
import at.uastw.fishdiary.db.FishesDao
import kotlinx.coroutines.flow.map

class FishRepository(private val fishesDao: FishesDao) {

    val fishes = fishesDao.getAllFishes().map { fishList ->
        fishList.map { entity ->
            Fish(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                categories = entity.categories,
                hasSeen = entity.hasSeen,
                imagePath = entity.imagePath
            )
        }
    }

    suspend fun addFish(
        name: String,
        description: String,
        categories: String,
        hasSeen: Boolean,
        imagePath: String?
    ) {
        fishesDao.addFish(
            FishEntity(
                id = 0,
                name = name,
                description = description,
                categories = categories,
                hasSeen = hasSeen,
                imagePath = imagePath
            )
        )
    }

    suspend fun updateFish(fish: Fish) {
        fishesDao.updateFish(
            FishEntity(
                id = fish.id,
                name = fish.name,
                description = fish.description,
                categories = fish.categories,
                hasSeen = fish.hasSeen,
                imagePath = fish.imagePath
            )
        )
    }

    suspend fun findFishById(fishId: Int): Fish {
        val e = fishesDao.findFishById(fishId)
        return Fish(e.id, e.name, e.description, e.categories, e.hasSeen, e.imagePath)
    }

    suspend fun deleteFish(fish: Fish) {
        fishesDao.deleteFish(
            FishEntity(
                id = fish.id,
                name = fish.name,
                description = fish.description,
                categories = fish.categories,
                hasSeen = fish.hasSeen,
                imagePath = fish.imagePath
            )
        )
    }
}

