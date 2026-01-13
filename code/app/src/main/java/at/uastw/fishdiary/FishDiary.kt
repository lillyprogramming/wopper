package at.uastw.fishdiary

import android.app.Application
import at.uastw.fishdiary.data.FishRepository
import at.uastw.fishdiary.db.FishesDatabase

class FishDiary : Application() {
    val fishRepository by lazy {

        val fishesDao = FishesDatabase.getDatabase(this).fishesDao()
        FishRepository(fishesDao)
    }
}