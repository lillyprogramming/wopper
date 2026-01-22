package at.uastw.wopper.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipesDao {

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

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

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Int)

    @Query("DELETE FROM instructions WHERE recipeId = :recipeId")
    suspend fun deleteInstructionsByRecipeId(recipeId: Int)

}
