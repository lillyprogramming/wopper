package at.uastw.fishdiary.data

import at.uastw.fishdiary.db.*
import kotlinx.coroutines.flow.map

class RecipeRepository(private val recipesDao: RecipesDao) {

    val recipes = recipesDao.getAllRecipesWithDetails().map { list ->
        list.map { row ->
            Recipe(
                id = row.recipe.id,
                mealType = row.recipe.mealType,
                categories = row.recipe.categories,
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
                notes = row.recipe.notes,
                totalTime = row.recipe.totalTime,
                difficulty = row.recipe.difficulty,
                servingSize = row.recipe.servingSize,
            )
        }
    }

    suspend fun addRecipe(
        mealType: String,
        categories: String,
        name: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        notes: String,
        totalTime: Int,
        difficulty: Int,
        servingSize: Int
    ) {
        val recipeId = recipesDao.insertRecipe(
            RecipeEntity(mealType = mealType, categories = categories, name = name, imagePath = imagePath, notes = notes, totalTime = totalTime, difficulty = difficulty, servingSize = servingSize)
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
            categories = row.recipe.categories,
            name = row.recipe.name,
            imagePath = row.recipe.imagePath,
            ingredients = row.ingredients.map {
                Ingredient(it.id, it.recipeId, it.name, it.amount, it.unit)
            },
            instructions = row.instructions.sortedBy { it.stepNumber }.map {
                Instruction(it.id, it.recipeId, it.stepNumber, it.text, it.timer)
            },
            notes = row.recipe.notes,
            totalTime = row.recipe.totalTime,
            difficulty = row.recipe.difficulty,
            servingSize = row.recipe.servingSize,
        )
    }

    suspend fun deleteRecipe(id: Int) {
        recipesDao.deleteById(id)
    }

    suspend fun updateRecipe(
        id: Int,
        mealType: String,
        categories: String,
        name: String,
        imagePath: String?,
        ingredients: List<Ingredient>,
        instructions: List<Instruction>,
        notes: String,
        totalTime: Int,
        difficulty: Int,
        servingSize: Int
    ) {
        recipesDao.updateRecipe(
            RecipeEntity(
                id = id,
                mealType = mealType,
                categories = categories,
                name = name,
                imagePath = imagePath,
                notes = notes,
                totalTime = totalTime,
                difficulty = difficulty,
                servingSize = servingSize,
            )
        )

        recipesDao.deleteIngredientsByRecipeId(id)
        recipesDao.deleteInstructionsByRecipeId(id)

        recipesDao.insertIngredients(
            ingredients.map {
                IngredientEntity(
                    recipeId = id,
                    name = it.name,
                    amount = it.amount,
                    unit = it.unit
                )
            }
        )

        recipesDao.insertInstructions(
            instructions.map {
                InstructionEntity(
                    recipeId = id,
                    stepNumber = it.stepNumber,
                    text = it.text,
                    timer = it.timer
                )
            }
        )
    }

}
