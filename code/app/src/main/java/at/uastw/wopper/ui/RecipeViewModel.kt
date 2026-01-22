package at.uastw.wopper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.uastw.wopper.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RecipesViewModel(
    val repository: RecipeRepository
) : ViewModel() {

    val recipesUiState = repository.recipes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
