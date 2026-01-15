package at.uastw.fishdiary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Ingredient
import at.uastw.fishdiary.data.Instruction
import at.uastw.fishdiary.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeEditUiState(
    val recipeId: Int = 0,
    val name: String = "",
    val mealType: String = "",
    val categories: List<String> = emptyList(),
    val totalTime: String = "",
    val difficulty: String = "",
    val ingredientsText: String = "",
    val instructionsText: String = "",
    val imagePath: String? = null
)

class RecipeEditViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository
) : ViewModel() {

    private val recipeId: Int = savedStateHandle["recipeId"] ?: 0

    private val _uiState = MutableStateFlow(RecipeEditUiState(recipeId = recipeId))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val r = repository.getRecipeById(recipeId) ?: return@launch
            _uiState.update {
                it.copy(
                    name = r.name,
                    mealType = r.mealType,
                    categories = r.categories.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() },
                    totalTime = r.totalTime.toString(),
                    difficulty = r.difficulty.toString(),
                    ingredientsText = r.ingredients.joinToString("\n") { ing ->
                        listOfNotNull(ing.amount, ing.unit, ing.name).joinToString(" ")
                    },
                    instructionsText = r.instructions.sortedBy { it.stepNumber }.joinToString("\n") { step ->
                        step.text
                    },
                    imagePath = r.imagePath
                )
            }
        }
    }

    fun updateName(v: String) = _uiState.update { it.copy(name = v) }
    fun updateMealType(v: String) = _uiState.update { it.copy(mealType = v) }
    fun updateCategories(v: List<String>) = _uiState.update { it.copy(categories = v) }
    fun updateTotalTime(v: String) = _uiState.update { it.copy(totalTime = v) }
    fun updateDifficulty(v: String) = _uiState.update { it.copy(difficulty = v) }
    fun updateIngredientsText(v: String) = _uiState.update { it.copy(ingredientsText = v) }
    fun updateInstructionsText(v: String) = _uiState.update { it.copy(instructionsText = v) }
    fun updateImagePath(v: String?) = _uiState.update { it.copy(imagePath = v) }

    fun save(onFinished: () -> Unit) {
        val s = _uiState.value
        viewModelScope.launch {
            val total = s.totalTime.toIntOrNull() ?: 0
            val diff = s.difficulty.toIntOrNull() ?: 1

            val ingredients = parseIngredientsLines(s.ingredientsText)
            val instructions = parseInstructionsLines(s.instructionsText)

            repository.updateRecipe(
                id = s.recipeId,
                mealType = s.mealType,
                categories = s.categories.joinToString(","),
                name = s.name,
                imagePath = s.imagePath,
                ingredients = ingredients,
                instructions = instructions,
                totalTime = total,
                difficulty = diff
            )
            onFinished()
        }
    }

    private fun parseIngredientsLines(text: String): List<Ingredient> =
        text.lines().map { it.trim() }.filter { it.isNotBlank() }.map { line ->
            val parts = line.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 3) Ingredient(0, 0, parts.drop(2).joinToString(" "), parts[0], parts[1])
            else Ingredient(0, 0, line, null, null)
        }

    private fun parseInstructionsLines(text: String): List<Instruction> =
        text.lines().map { it.trim() }.filter { it.isNotBlank() }.mapIndexed { idx, line ->
            Instruction(0, 0, idx + 1, line, 0)
        }
}
