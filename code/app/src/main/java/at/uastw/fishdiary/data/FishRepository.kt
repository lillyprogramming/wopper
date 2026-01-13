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
                mealType = entity.mealType,
                instructions = entity.instructions,
                ingredients = entity.ingredients,
                totalTime = entity.totalTime,
                imagePath = entity.imagePath
            )
        }
    }

    suspend fun addFish(
        name: String,
        mealType: String,
        instructions: String,
        ingredients: String,
        totalTime: Int,
        imagePath: String?
    ) {
        fishesDao.addFish(
            FishEntity(
                id = 0,
                name = name,
                mealType = mealType,
                instructions = instructions,
                ingredients = ingredients,
                totalTime = totalTime,
                imagePath = imagePath
            )
        )
    }

    suspend fun updateFish(recipe: Recipe) {
        fishesDao.updateFish(
            FishEntity(
                id = recipe.id,
                name = recipe.name,
                mealType = recipe.mealType,
                instructions = recipe.instructions,
                ingredients = recipe.ingredients,
                totalTime = recipe.totalTime,
                imagePath = recipe.imagePath,
            )
        )
    }

    suspend fun findFishById(fishId: Int): Recipe {
        val e = fishesDao.findFishById(fishId)
        return Recipe(e.id, e.name,e.mealType, e.instructions, e.ingredients, e.totalTime, e.imagePath)
    }

    suspend fun deleteFish(recipe: Recipe) {
        fishesDao.deleteFish(
            FishEntity(
                id = recipe.id,
                name = recipe.name,
                mealType = recipe.mealType,
                instructions = recipe.instructions,
                ingredients = recipe.ingredients,
                totalTime = recipe.totalTime,
                imagePath = recipe.imagePath
            )
        )
    }
}

