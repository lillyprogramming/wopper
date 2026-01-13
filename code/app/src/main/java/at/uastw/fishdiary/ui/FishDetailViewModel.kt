package at.uastw.fishdiary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.Fish
import at.uastw.fishdiary.data.FishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class  FishDetailUiState(
    val fish: Fish
)

class FishDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: FishRepository ) : ViewModel()
{
    private val fishId: Int = savedStateHandle["fishId"] ?: 0
    private val _fishDetailUiState = MutableStateFlow(FishDetailUiState(Fish(0, "", "", "", false)))
    val fishDetailUiState = _fishDetailUiState.asStateFlow()

    fun onDeleteFish(onFinished: () -> Unit) {
        val fish = _fishDetailUiState.value.fish
        viewModelScope.launch {
            repository.deleteFish(fish)
            onFinished()
        }
    }

    init{

        viewModelScope.launch {
            val fish = repository.findFishById(fishId)
            _fishDetailUiState.update {
                it.copy(fish = fish)
            }
        }
    }

}

