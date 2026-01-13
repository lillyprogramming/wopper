package at.uastw.fishdiary.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipesDao {

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long  // returns new id

    @Insert
    suspend fun insertIngredients(items: List<IngredientEntity>)

    @Insert
    suspend fun insertInstructions(items: List<InstructionEntity>)

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipesWithDetails(): Flow<List<FullRecipe>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeWithDetailsById(id: Int): FullRecipe?

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
