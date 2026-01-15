package at.uastw.fishdiary.ui

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
        name: String,
        mealType: String,
        categories: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        totalTime: Int,
        difficulty: Int,
    ) {
        viewModelScope.launch {
            repository.addRecipe(
                name = name,
                mealType = mealType,
                categories = categories,
                imagePath = imagePath,
                ingredients = ingredients,
                instructions = instructions,
                totalTime = totalTime,
                difficulty = difficulty
            )
        }
    }
}
