package at.uastw.fishdiary.data

import at.uastw.fishdiary.db.*
import kotlinx.coroutines.flow.map

class RecipeRepository(private val recipesDao: RecipesDao) {

    val recipes = recipesDao.getAllRecipesWithDetails().map { list ->
        list.map { row ->
            Recipe(
                id = row.recipe.id,
                mealType = row.recipe.mealType,
                name = row.recipe.name,
                imagePath = row.recipe.imagePath,
                ingredients = row.ingredients.map {
                    Ingredient(it.id, it.recipeId, it.name, it.amount, it.unit)
                },
                instructions = row.instructions
                    .sortedBy { it.stepNumber }
                    .map {
                        Instruction(it.id, it.recipeId, it.stepNumber, it.text, it.timer)
                    },
                totalTime = row.recipe.totalTime,
                difficulty = row.recipe.difficulty,
            )
        }
    }

    suspend fun addRecipe(
        mealType: String,
        name: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        totalTime: Int,
        difficulty: Int,
    ) {
        val recipeId = recipesDao.insertRecipe(
            RecipeEntity(mealType = mealType, name = name, imagePath = imagePath, totalTime = totalTime, difficulty = difficulty)
        ).toInt()

        recipesDao.insertIngredients(
            ingredients.map {
                IngredientEntity(
                    recipeId = recipeId,
                    name = it.name,
                    amount = it.amount,
                    unit = it.unit
                )
            }
        )

        recipesDao.insertInstructions(
            instructions.map {
                InstructionEntity(
                    recipeId = recipeId,
                    stepNumber = it.stepNumber,
                    text = it.text,
                    timer = it.timer
                )
            }
        )
    }

    suspend fun getRecipeById(id: Int): Recipe? {
        val row = recipesDao.getRecipeWithDetailsById(id) ?: return null
        return Recipe(
            id = row.recipe.id,
            mealType = row.recipe.mealType,
            name = row.recipe.name,
            imagePath = row.recipe.imagePath,
            ingredients = row.ingredients.map {
                Ingredient(it.id, it.recipeId, it.name, it.amount, it.unit)
            },
            instructions = row.instructions.sortedBy { it.stepNumber }.map {
                Instruction(it.id, it.recipeId, it.stepNumber, it.text, it.timer)
            },
            totalTime = row.recipe.totalTime,
            difficulty = row.recipe.difficulty,
        )
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipesDao.deleteRecipe(RecipeEntity(recipe.id, recipe.mealType, recipe.name, recipe.imagePath, recipe.totalTime, recipe.difficulty))
    }
}
