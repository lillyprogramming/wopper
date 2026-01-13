package at.uastw.fishdiary.data

import at.uastw.fishdiary.db.FishEntity
import at.uastw.fishdiary.db.FishesDao
import kotlinx.coroutines.flow.map

class FishRepository(private val fishesDao: FishesDao) {

    val fishes = fishesDao.getAllFishes().map { fishList ->
        fishList.map { entity ->
            Recipe(
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

    suspend fun updateFish(recipe: Recipe) {
        fishesDao.updateFish(
            FishEntity(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                categories = recipe.categories,
                hasSeen = recipe.hasSeen,
                imagePath = recipe.imagePath
            )
        )
    }

    suspend fun findFishById(fishId: Int): Recipe {
        val e = fishesDao.findFishById(fishId)
        return Recipe(e.id, e.name, e.description, e.categories, e.hasSeen, e.imagePath)
    }

    suspend fun deleteFish(recipe: Recipe) {
        fishesDao.deleteFish(
            FishEntity(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                categories = recipe.categories,
                hasSeen = recipe.hasSeen,
                imagePath = recipe.imagePath
            )
        )
    }
}

