package at.uastw.fishdiary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.fishdiary.data.FishRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishesViewModel(
    val repository: FishRepository
) : ViewModel() {

    val fishesUiState = repository.fishes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addFish(
        name: String,
        description: String,
        categories: String,
        hasSeen: Boolean,
        imagePath: String?
    ) {
        viewModelScope.launch {
            repository.addFish(name, description, categories, hasSeen, imagePath)
        }
    }
}
