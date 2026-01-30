package at.uastw.wopper.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.uastw.wopper.Wopper

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as Wopper
            RecipesViewModel(app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as Wopper
            RecipeDetailViewModel(this.createSavedStateHandle(), app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as Wopper
            RecipeEditViewModel(this.createSavedStateHandle(), app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as Wopper
            AddRecipeViewModel(app.recipeRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as Wopper
            TimerViewModel(app)
        }
    }
}


