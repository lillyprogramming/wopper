package at.uastw.fishdiary

import android.app.Application
import at.uastw.fishdiary.data.RecipeRepository
import at.uastw.fishdiary.db.RecipesDatabase

class FishDiary : Application() {
    val recipeRepository by lazy {

        val recipesDao = RecipesDatabase.getDatabase(this).recipesDao()
        RecipeRepository(recipesDao)
    }
}