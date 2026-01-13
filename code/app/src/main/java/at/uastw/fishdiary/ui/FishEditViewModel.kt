package at.uastw.fishdiary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Recipe
import at.uastw.fishdiary.data.FishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FishEditUiState(
    val recipe: Recipe = Recipe(0, "", "", "", false, null)
)

class FishEditViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: FishRepository
) : ViewModel() {

    private val fishId: Int = savedStateHandle["fishId"] ?: 0
    private val _uiState = MutableStateFlow(FishEditUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val fish = repository.findFishById(fishId)
            _uiState.update { it.copy(recipe = fish) }
        }
    }

    fun updateName(v: String) = _uiState.update { it.copy(recipe = it.recipe.copy(name = v)) }
    fun updateDescription(v: String) = _uiState.update { it.copy(recipe = it.recipe.copy(description = v)) }
    fun updateCategories(v: String) = _uiState.update { it.copy(recipe = it.recipe.copy(categories = v)) }
    fun updateHasSeen(v: Boolean) = _uiState.update { it.copy(recipe = it.recipe.copy(hasSeen = v)) }
    fun updateImagePath(v: String?) = _uiState.update { it.copy(recipe = it.recipe.copy(imagePath = v)) }

    fun save(onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.updateFish(_uiState.value.recipe)
            onFinished()
        }
    }
}
