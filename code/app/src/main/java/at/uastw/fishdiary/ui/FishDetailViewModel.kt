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

data class  FishDetailUiState(
    val recipe: Recipe
)

class FishDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: FishRepository ) : ViewModel()
{
    private val fishId: Int = savedStateHandle["fishId"] ?: 0
    private val _recipeDetailUiState = MutableStateFlow(FishDetailUiState(Recipe(0, "", "", "", false)))
    val fishDetailUiState = _recipeDetailUiState.asStateFlow()

    fun onDeleteFish(onFinished: () -> Unit) {
        val fish = _recipeDetailUiState.value.recipe
        viewModelScope.launch {
            repository.deleteFish(fish)
            onFinished()
        }
    }

    init{

        viewModelScope.launch {
            val fish = repository.findFishById(fishId)
            _recipeDetailUiState.update {
                it.copy(recipe = fish)
            }
        }
    }

}

