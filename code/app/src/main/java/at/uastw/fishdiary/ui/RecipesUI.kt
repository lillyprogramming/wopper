package at.uastw.fishdiary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.uastw.fishdiary.data.Ingredient
import at.uastw.fishdiary.data.Instruction
import at.uastw.fishdiary.data.Recipe
import at.uastw.fishdiary.ui.recipes.RecipeDetailViewModel
import at.uastw.fishdiary.ui.recipes.RecipesViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

enum class Routes(val route: String) {
    List("list"),
    Detail("detail/{recipeId}")
}

private val mealTypeArr = listOf(
    "Breakfast",
    "Brunch",
    "Lunch",
    "Dinner",
    "Dessert",
    "Drinks",
    "Salads",
    "Side Dishes",
    "Soups",
    "Snacks"
)

@Composable
fun FishDiaryApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Routes.List.route
    ) {
        composable(Routes.List.route) {
            RecipesListView { recipeId ->
                navController.navigate("detail/$recipeId")
            }
        }

        composable(
            Routes.Detail.route,
            listOf(navArgument("recipeId") { type = NavType.IntType })
        ) {
            RecipeDetailView(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeDropdown(
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = mealType,
            onValueChange = {}, // readOnly dropdown
            readOnly = true,
            label = { Text("Meal type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            mealTypeArr.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onMealTypeChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RecipesListView(
    modifier: Modifier = Modifier,
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onRecipeClick: (Int) -> Unit
) {
    val recipes by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()

    var mealType by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var totalTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    var instructionsText by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imagePath = uri.toString()
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recipes", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        MealTypeDropdown(
            mealType = mealType,
            onMealTypeChange = { mealType = it },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = totalTime,
                onValueChange = { totalTime = it },
                label = { Text("Total time (min)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = difficulty,
                onValueChange = { difficulty = it },
                label = { Text("Difficulty (1-5)") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = ingredientsText,
            onValueChange = { ingredientsText = it },
            label = { Text("Ingredients (one per line)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = instructionsText,
            onValueChange = { instructionsText = it },
            label = { Text("Instructions (one step per line)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text("Image (optional)", style = MaterialTheme.typography.titleMedium)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { pickImageLauncher.launch(arrayOf("image/*")) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Pick Image")
            }

            OutlinedButton(
                onClick = { imagePath = null },
                enabled = imagePath != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Remove")
            }
        }

        if (imagePath != null) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Selected recipe image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (mealType.isBlank() || name.isBlank()) return@Button

                val total = totalTime.toIntOrNull() ?: 0
                val diff = difficulty.toIntOrNull() ?: 1

                val ingredients = parseIngredientsLines(ingredientsText)
                val instructions = parseInstructionsLines(instructionsText)

                recipesViewModel.addRecipe(
                    mealType = mealType,
                    name = name,
                    imagePath = imagePath,
                    ingredients = ingredients,
                    instructions = instructions,
                    totalTime = total,
                    difficulty = diff
                )

                mealType = ""
                name = ""
                totalTime = ""
                difficulty = ""
                ingredientsText = ""
                instructionsText = ""
                imagePath = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Recipe")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(recipes) { _, recipe ->
                RecipeListItem(
                    recipe = recipe,
                    onCardClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}


@Composable
fun RecipeListItem(
    recipe: Recipe,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("${recipe.mealType} • ${recipe.totalTime} min • Diff ${recipe.difficulty}")
        }
    }
}

@Composable
fun RecipeDetailView(
    recipeDetailViewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController? = null
) {
    val state by recipeDetailViewModel.recipeDetailUiState.collectAsStateWithLifecycle()

    RecipeDetails(
        recipe = state.recipe,
        onBackClick = { navController?.popBackStack() },
        onDeleteClick = {
            recipeDetailViewModel.onDeleteRecipe {
                navController?.popBackStack()
            }
        }
    )
}

@Composable
fun RecipeDetails(
    recipe: Recipe,
    onDeleteClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {

            Text(recipe.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("${recipe.mealType} • ${recipe.totalTime} min • Difficulty ${recipe.difficulty}")

            Spacer(Modifier.height(16.dp))

            if (!recipe.imagePath.isNullOrBlank()) {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = "Recipe image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(12.dp))
            }


            Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
            recipe.ingredients.forEach { ing ->
                val amountUnit = listOfNotNull(ing.amount?.takeIf { it.isNotBlank() }, ing.unit?.takeIf { it.isNotBlank() })
                    .joinToString(" ")
                Text("• ${amountUnit.ifBlank { "" }} ${ing.name}".trim())
            }

            Spacer(Modifier.height(12.dp))

            Text("Instructions:", style = MaterialTheme.typography.titleMedium)
            recipe.instructions.sortedBy { it.stepNumber }.forEach { step ->
                Text("${step.stepNumber}. ${step.text}")
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Go Back") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete Recipe") }
        }
    }
}
private fun parseIngredientsLines(text: String): List<Ingredient> {
    return text
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 3) {
                Ingredient(
                    recipeId = 0,
                    name = parts.drop(2).joinToString(" "),
                    amount = parts[0],
                    unit = parts[1]
                )
            } else {
                Ingredient(
                    recipeId = 0,
                    name = line,
                    amount = null,
                    unit = null
                )
            }
        }
}

private fun parseInstructionsLines(text: String): List<Instruction> {
    return text
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .mapIndexed { idx, line ->
            Instruction(
                recipeId = 0,
                stepNumber = idx + 1,
                text = line,
                timer = 0
            )
        }
}
