package at.uastw.wopper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.wopper.data.Ingredient
import at.uastw.wopper.data.Instruction
import at.uastw.wopper.data.RecipeRepository
import kotlinx.coroutines.launch

class AddRecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    fun addRecipe(
        name: String,
        mealType: String,
        categories: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        notes: String,
        totalTime: Int,
        difficulty: Int,
        servingSize: Int,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addRecipe(
                mealType = mealType,
                categories = categories,
                name = name,
                imagePath = imagePath,
                ingredients = ingredients,
                instructions = instructions,
                notes = notes,
                totalTime = totalTime,
                difficulty = difficulty,
                servingSize = servingSize,
            )
            onFinished()
        }
    }
}
