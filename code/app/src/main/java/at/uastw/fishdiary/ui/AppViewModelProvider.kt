package at.uastw.fishdiary.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.uastw.fishdiary.FishDiary

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            FishesViewModel(app.fishRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            FishDetailViewModel(this.createSavedStateHandle(), app.fishRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as FishDiary
            FishEditViewModel(this.createSavedStateHandle(), app.fishRepository)
        }
    }
}
