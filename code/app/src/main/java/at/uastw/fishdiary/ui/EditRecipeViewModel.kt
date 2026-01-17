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
    val loaded: Boolean = false,
    val recipeId: Int = 0,
    val name: String = "",
    val mealType: String = "",
    val categories: List<String> = emptyList(),
    val totalTime: String = "",
    val difficulty: String = "",
    val notes: String = "",
    val imagePath: String? = null,
    val ingredients: List<IngredientDraft> = emptyList(),
    val instructions: List<InstructionDraft> = emptyList()
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
                    loaded = true,
                    name = r.name,
                    mealType = r.mealType,
                    categories = r.categories.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() },
                    totalTime = r.totalTime.toString(),
                    difficulty = r.difficulty.toString(),
                    notes = r.notes,
                    imagePath = r.imagePath,
                    ingredients = r.ingredients.map { ing ->
                        IngredientDraft(
                            amount = ing.amount ?: "",
                            unit = ing.unit ?: "",
                            name = ing.name
                        )
                    },
                    instructions = r.instructions
                        .sortedBy { it.stepNumber }
                        .map { step ->
                            InstructionDraft(
                                text = step.text,
                                timer = step.timer.takeIf { t -> t > 0 }?.toString() ?: ""
                            )
                        }
                )
            }
        }
    }

    fun updateName(v: String) = _uiState.update { it.copy(name = v) }
    fun updateMealType(v: String) = _uiState.update { it.copy(mealType = v) }
    fun updateCategories(v: List<String>) = _uiState.update { it.copy(categories = v) }
    fun updateTotalTime(v: String) = _uiState.update { it.copy(totalTime = v) }
    fun updateDifficulty(v: String) = _uiState.update { it.copy(difficulty = v) }
    fun updateImagePath(v: String?) = _uiState.update { it.copy(imagePath = v) }
    fun updateNotes(v: String) = _uiState.update { it.copy(notes = v) }

    fun save(
        ingredientDrafts: List<IngredientDraft>,
        instructionDrafts: List<InstructionDraft>,
        onFinished: () -> Unit
    ) {
        val s = _uiState.value
        viewModelScope.launch {
            val total = s.totalTime.toIntOrNull() ?: 0
            val diff = s.difficulty.toIntOrNull() ?: 1

            val ingredients = ingredientDrafts
                .filter { it.name.isNotBlank() }
                .map { d ->
                    Ingredient(
                        recipeId = s.recipeId,
                        name = d.name.trim(),
                        amount = d.amount.trim().ifBlank { null },
                        unit = d.unit.trim().ifBlank { null }
                    )
                }

            val instructions = instructionDrafts
                .filter { it.text.isNotBlank() }
                .mapIndexed { idx, d ->
                    Instruction(
                        recipeId = s.recipeId,
                        stepNumber = idx + 1,
                        text = d.text.trim(),
                        timer = d.timer.toIntOrNull() ?: 0
                    )
                }

            repository.updateRecipe(
                id = s.recipeId,
                mealType = s.mealType,
                categories = s.categories.joinToString(","),
                name = s.name,
                imagePath = s.imagePath,
                ingredients = ingredients,
                instructions = instructions,
                notes = s.notes,
                totalTime = total,
                difficulty = diff
            )
            onFinished()
        }
    }

    fun delete(onFinished: () -> Unit) {
        val id = _uiState.value.recipeId
        viewModelScope.launch {
            repository.deleteRecipe(id)
            onFinished()
        }
    }
}
