package at.uastw.fishdiary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Recipe
import at.uastw.fishdiary.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe
)

class RecipeDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository
) : ViewModel() {

    private val recipeId: Int = savedStateHandle["recipeId"] ?: 0

    private val _recipeDetailUiState = MutableStateFlow(
        RecipeDetailUiState(
            Recipe(
                id = 0,
                mealType = "",
                categories = "",
                name = "",
                imagePath = null,
                ingredients = emptyList(),
                instructions = emptyList(),
                totalTime = 0,
                difficulty = 1
            )
        )
    )
    val recipeDetailUiState = _recipeDetailUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val recipe = repository.getRecipeById(recipeId)
            if (recipe != null) {
                _recipeDetailUiState.update { it.copy(recipe = recipe) }
            }
        }
    }
}
