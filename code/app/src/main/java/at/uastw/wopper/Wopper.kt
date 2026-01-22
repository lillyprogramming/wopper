package at.uastw.wopper

import android.app.Application
import at.uastw.wopper.data.RecipeRepository
import at.uastw.wopper.db.RecipesDatabase

class Wopper : Application() {
    val recipeRepository by lazy {

        val recipesDao = RecipesDatabase.getDatabase(this).recipesDao()
        RecipeRepository(recipesDao)
    }
}