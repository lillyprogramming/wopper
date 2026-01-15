package at.uastw.fishdiary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.uastw.fishdiary.data.Ingredient
import at.uastw.fishdiary.data.Instruction
import at.uastw.fishdiary.data.Recipe
import at.uastw.fishdiary.ui.recipes.RecipeDetailViewModel
import at.uastw.fishdiary.ui.recipes.RecipesViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

enum class Routes(val route: String) {
    List("list"),
    Detail("detail/{recipeId}"),
    Add("add"),  // Added route for add screen
    Edit("edit/{recipeId}")  // Added route for edit screen
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
            WoopperHomepageView(
                navController = navController,
                onRecipeClick = { recipeId ->
                    navController.navigate("detail/$recipeId")
                }
            )
        }

        composable(
            Routes.Detail.route,
            listOf(navArgument("recipeId") { type = NavType.IntType })
        ) {
            WoopperDetailsView(navController = navController)
        }

        composable(Routes.Add.route) {
            AddRecipeView(navController = navController)
        }

        composable(
            Routes.Edit.route,
            listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0
            EditRecipeView(
                navController = navController,
                recipeId = recipeId
            )
        }
    }
}

// Woopper Style Homepage
@Composable
fun WoopperHomepageView(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onRecipeClick: (Int) -> Unit
) {
    val recipes by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()

    // Group recipes by meal type
    val recipesByMealType = recipes.groupBy { it.mealType }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7AF9D))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Header with light blue background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB0D0D3))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "wopper",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = Color(0xFFFFCAD4)
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            // Create a section for each meal type that has recipes
            mealTypeArr.forEach { mealType ->
                val mealTypeRecipes = recipesByMealType[mealType] ?: emptyList()
                
                // Only show sections that have at least one recipe
                if (mealTypeRecipes.isNotEmpty()) {
                    item {
                        MealTypeSection(
                            mealType = mealType,
                            recipes = mealTypeRecipes,
                            onRecipeClick = onRecipeClick
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }

        // Floating Action Button for adding recipes
        FloatingActionButton(
            onClick = {
                navController.navigate(Routes.Add.route)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFC08497),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Recipe"
            )
        }
    }
}

@Composable
fun MealTypeSection(
    mealType: String,
    recipes: List<Recipe>,
    onRecipeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "# $mealType",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val randomRecipe = recipes.random()
                    onRecipeClick(randomRecipe.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB0D0D3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Pick a Random one",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            // Create a grid layout using rows
            recipes.chunked(2).forEach { rowRecipes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowRecipes.forEach { recipe ->
                        Box(modifier = Modifier.weight(1f)) {
                            WoopperRecipeCard(
                                recipe = recipe,
                                onCardClick = { onRecipeClick(recipe.id) }
                            )
                        }
                    }
                    // Add empty space if odd number of items
                    if (rowRecipes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun WoopperRecipeCard(
    recipe: Recipe,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE0E6)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFFFB3C1)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recipe Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (recipe.imagePath != null && recipe.imagePath.isNotBlank()) {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = recipe.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipe.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
            
            // Recipe Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7AF9D))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

// Woopper Style Details Page
@Composable
fun WoopperDetailsView(
    recipeDetailViewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController? = null
) {
    val state by recipeDetailViewModel.recipeDetailUiState.collectAsStateWithLifecycle()

    WoopperRecipeDetails(
        recipe = state.recipe,
        onBackClick = { navController?.popBackStack() },
        onEditClick = {
            navController?.navigate("edit/${state.recipe.id}")
        }
    )
}

@Composable
fun WoopperRecipeDetails(
    recipe: Recipe,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(24.dp))
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${recipe.mealType} • ${recipe.totalTime} min • Difficulty ${recipe.difficulty}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6B7280)
                    )
                }
            }

        // Recipe Image
        if (!recipe.imagePath.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = "Recipe image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Ingredients Section
        item {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Ingredients:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (recipe.ingredients.isEmpty()) {
                    Text(
                        text = "No ingredients listed",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recipe.ingredients.forEach { ingredient ->
                        val amountUnit = listOfNotNull(
                            ingredient.amount?.takeIf { it.isNotBlank() },
                            ingredient.unit?.takeIf { it.isNotBlank() }
                        ).joinToString(" ")
                        Text(
                            text = "• ${amountUnit.ifBlank { "" }} ${ingredient.name}".trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Instructions Section
        item {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (recipe.instructions.isEmpty()) {
                    Text(
                        text = "No instructions listed",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recipe.instructions.sortedBy { it.stepNumber }.forEachIndexed { index, step ->
                        Text(
                            text = "${index + 1}. ${step.text}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        // Timer Button
        item {
            Button(
                onClick = { /* TODO: Implement timer */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                )
            ) {
                Text(
                    text = "Set timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        // Back Button (moved to top bar)
        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
        }

        // Top bar with back arrow and gear icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111827)
                )
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit Recipe",
                    tint = Color(0xFF111827)
                )
            }
        }
    }
}

// Add Recipe Screen
@Composable
fun AddRecipeView(
    navController: NavHostController,
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Add New Recipe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance spacing
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            MealTypeDropdown(
                mealType = mealType,
                onMealTypeChange = { mealType = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
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
        }

        item {Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                label = { Text("Ingredients") },
                modifier = Modifier.weight(0.7f)   // 70%
            )

            OutlinedTextField(
                value = difficulty,
                onValueChange = { difficulty = it },
                label = { Text("Amount") },
                modifier = Modifier.weight(0.3f)   // 30%
            )

            }
        }

        item {
            OutlinedTextField(
                value = instructionsText,
                onValueChange = { instructionsText = it },
                label = { Text("Instructions (one step per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
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
        }

        if (imagePath != null) {
            item {
                Card(Modifier.fillMaxWidth()) {
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
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
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

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && mealType.isNotBlank()
            ) {
                Text("Save Recipe", fontSize = 18.sp)
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

// Keep the existing functions
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
            onValueChange = {},
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

// Edit Recipe Screen
@Composable
fun EditRecipeView(
    navController: NavHostController,
    recipeId: Int,
    recipeDetailViewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by recipeDetailViewModel.recipeDetailUiState.collectAsStateWithLifecycle()
    val recipe = state.recipe

    var mealType by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var totalTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    var instructionsText by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    // Update state when recipe loads
    LaunchedEffect(recipe.id) {
        if (recipe.id != 0) {
            mealType = recipe.mealType
            name = recipe.name
            totalTime = recipe.totalTime.toString()
            difficulty = recipe.difficulty.toString()
            ingredientsText = recipe.ingredients.joinToString("\n") { ing ->
                val amountUnit = listOfNotNull(
                    ing.amount?.takeIf { it.isNotBlank() },
                    ing.unit?.takeIf { it.isNotBlank() }
                ).joinToString(" ")
                "${amountUnit.ifBlank { "" }} ${ing.name}".trim()
            }
            instructionsText = recipe.instructions.sortedBy { it.stepNumber }
                .joinToString("\n") { it.text }
            imagePath = recipe.imagePath
        }
    }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Recipe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance spacing
            }
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            MealTypeDropdown(
                mealType = mealType,
                onMealTypeChange = { mealType = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
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
        }

        item {
            OutlinedTextField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                label = { Text("Ingredients (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            OutlinedTextField(
                value = instructionsText,
                onValueChange = { instructionsText = it },
                label = { Text("Instructions (one step per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
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
        }

        if (imagePath != null) {
            item {
                Card(Modifier.fillMaxWidth()) {
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
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Button(
                onClick = {
                    if (mealType.isBlank() || name.isBlank()) return@Button

                    val total = totalTime.toIntOrNull() ?: 0
                    val diff = difficulty.toIntOrNull() ?: 1

                    val ingredients = parseIngredientsLines(ingredientsText)
                    val instructions = parseInstructionsLines(instructionsText)

                    recipesViewModel.updateRecipe(
                        recipeId = recipe.id,
                        mealType = mealType,
                        name = name,
                        imagePath = imagePath,
                        ingredients = ingredients,
                        instructions = instructions,
                        totalTime = total,
                        difficulty = diff
                    )

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && mealType.isNotBlank()
            ) {
                Text("Update Recipe", fontSize = 18.sp)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }

        // Delete Button
        item {
            Button(
                onClick = {
                    recipeDetailViewModel.onDeleteRecipe {
                        navController.popBackStack()
                        navController.popBackStack() // Pop back to list after deleting
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = "Delete Recipe",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
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