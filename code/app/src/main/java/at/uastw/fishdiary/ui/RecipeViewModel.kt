package at.uastw.fishdiary.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Ingredient
import at.uastw.fishdiary.data.Instruction
import at.uastw.fishdiary.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipesViewModel(
    val repository: RecipeRepository
) : ViewModel() {

    val recipesUiState = repository.recipes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addRecipe(
        mealType: String,
        name: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        totalTime: Int,
        difficulty: Int,
    ) {
        viewModelScope.launch {
            repository.addRecipe(
                mealType = mealType,
                name = name,
                imagePath = imagePath,
                ingredients = ingredients,
                instructions = instructions,
                totalTime = totalTime,
                difficulty = difficulty
            )
        }
    }

    fun updateRecipe(
        recipeId: Int,
        mealType: String,
        name: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        totalTime: Int,
        difficulty: Int,
    ) {
        viewModelScope.launch {
            repository.updateRecipe(
                recipeId = recipeId,
                mealType = mealType,
                name = name,
                imagePath = imagePath,
                ingredients = ingredients,
                instructions = instructions,
                totalTime = totalTime,
                difficulty = difficulty
            )
        }
    }
}
