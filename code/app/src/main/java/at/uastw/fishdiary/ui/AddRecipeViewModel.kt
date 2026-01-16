package at.uastw.fishdiary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Ingredient
import at.uastw.fishdiary.data.Instruction
import at.uastw.fishdiary.data.RecipeRepository
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
                difficulty = difficulty
            )
            onFinished()
        }
    }
}
