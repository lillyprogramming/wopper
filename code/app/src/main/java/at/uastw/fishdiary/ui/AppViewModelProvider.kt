package at.uastw.fishdiary.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.uastw.fishdiary.FishDiary

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            RecipesViewModel(app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            RecipeDetailViewModel(this.createSavedStateHandle(), app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            RecipeEditViewModel(this.createSavedStateHandle(), app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            AddRecipeViewModel(app.recipeRepository)
        }
    }
}


