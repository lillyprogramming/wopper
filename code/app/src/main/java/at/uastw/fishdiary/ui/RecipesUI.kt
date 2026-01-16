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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import android.net.Uri
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.result.PickVisualMediaRequest
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


enum class Routes(val route: String) {
    List("list"),
    Create("create"),
    Detail("detail/{recipeId}"),
    Edit("edit/{recipeId}")
}

data class IngredientDraft(
    val amount: String = "",
    val unit: String = "",
    val name: String = ""
)

data class InstructionDraft(
    val text: String = "",
    val timer: String = ""
)

private val unitArr = listOf(
    "g", "kg", "ml", "l", "tbsp", "tsp", "cup", "pcs", "pinch"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    unit: String,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = unit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            placeholder = { Text("Select") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            unitArr.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onUnitChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun IngredientsEditor(
    ingredients: List<IngredientDraft>,
    onAdd: (IngredientDraft) -> Unit,
    onRemoveAt: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Column(modifier) {
        Text("Ingredients", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.weight(1f)) {
                UnitDropdown(unit = unit, onUnitChange = { unit = it })
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ingredient") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (name.isBlank()) return@Button
                onAdd(IngredientDraft(amount = amount.trim(), unit = unit.trim(), name = name.trim()))
                amount = ""
                unit = ""
                name = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Ingredient") }

        if (ingredients.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ingredients.forEachIndexed { idx, ing ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val left = listOfNotNull(
                            ing.amount.takeIf { it.isNotBlank() },
                            ing.unit.takeIf { it.isNotBlank() }
                        ).joinToString(" ")

                        Text(
                            text = "${if (left.isBlank()) "" else "$left "} ${ing.name}".trim(),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(10.dp))

                        OutlinedButton(onClick = { onRemoveAt(idx) }) { Text("Remove") }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@Composable
private fun InstructionsEditor(
    instructions: List<InstructionDraft>,
    onAdd: (InstructionDraft) -> Unit,
    onRemoveAt: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf("") }

    Column(modifier) {
        Text("Instructions", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Step") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = timer,
            onValueChange = { timer = it },
            label = { Text("Timer (sec, optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (text.isBlank()) return@Button
                onAdd(InstructionDraft(text = text.trim(), timer = timer.trim()))
                text = ""
                timer = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Step") }

        if (instructions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            instructions.forEachIndexed { idx, step ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. ${step.text}" +
                                    (step.timer.toIntOrNull()?.let { if (it > 0) "  (${it}s)" else "" } ?: ""),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(onClick = { onRemoveAt(idx) }) { Text("Remove") }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
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

private val categoriesArr = listOf(
    "Fish",
    "Seafood",
    "Pasta",
    "Healthy",
    "Quick",
    "Low Carb",
    "Spicy",
    "Kids",
    "Vegetarian"
)


    @Composable
    fun FishDiaryApp(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
    ) {
        NavHost(navController = navController, modifier = modifier, startDestination = Routes.List.route) {

            composable(Routes.List.route) {
                RecipesHomeScreen(
                    onRecipeClick = { id -> navController.navigate("detail/$id") },
                    onAddClick = { navController.navigate(Routes.Create.route) }
                )
            }

            composable(Routes.Create.route) {
                CreateRecipeScreen(onFinished = { navController.popBackStack() })
            }

            composable(
                Routes.Detail.route,
                listOf(navArgument("recipeId") { type = NavType.IntType })
            ) {
                RecipeDetailView(
                    navController = navController,
                    onEditClick = { id -> navController.navigate("edit/$id") }
                )
            }

            composable(
                Routes.Edit.route,
                listOf(navArgument("recipeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0

                EditRecipeScreen(
                    onSaved = {
                        navController.navigate("detail/$recipeId") {
                            popUpTo("detail/$recipeId") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onDeleted = {
                        navController.navigate(Routes.List.route) {
                            popUpTo(Routes.List.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }


        }
    }

private fun parseCategoriesCsv(csv: String): List<String> =
    csv.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val dir = File(context.filesDir, "recipe_images").apply { mkdirs() }
            val outFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { output ->
                input.use { it.copyTo(output) }
            }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }


@Composable
fun ImagePickerField(
    label: String = "Image (optional)",
    existingImagePath: String?,
    pickedImageUri: Uri?,
    onPick: (Uri?) -> Unit,
    onRemoveExisting: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPick(uri)
    }

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.titleMedium)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (pickedImageUri == null) "Pick Image" else "Change") }

            OutlinedButton(
                onClick = {
                    onPick(null)
                    onRemoveExisting?.invoke()
                },
                enabled = pickedImageUri != null || (existingImagePath != null && onRemoveExisting != null),
                modifier = Modifier.weight(1f)
            ) { Text("Remove") }
        }

        val previewModel = when {
            pickedImageUri != null -> pickedImageUri
            !existingImagePath.isNullOrBlank() -> File(existingImagePath)
            else -> null
        }

        if (previewModel != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedCard(Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}



@Composable
private fun CategoriesChips(
    categoriesCsv: String,
    modifier: Modifier = Modifier,
    title: String = "Categories:"
) {
    val categoriesList = remember(categoriesCsv) { parseCategoriesCsv(categoriesCsv) }

    if (categoriesList.isEmpty()) return

    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categoriesList.forEach { cat ->
                AssistChip(
                    onClick = {},
                    label = { Text(cat) }
                )
            }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesMultiDropdown(
    selected: List<String>,
    onSelectedChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = remember(selected) {
        if (selected.isEmpty()) "" else selected.joinToString(", ")
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categories") },
            placeholder = { Text("Select categories") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categoriesArr.forEach { option ->
                val isChecked = selected.contains(option)

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    },
                    onClick = {
                        val newSelected =
                            if (isChecked) selected - option
                            else selected + option

                        onSelectedChange(newSelected)
                    }
                )
            }
        }
    }
}

@Composable
fun RecipesHomeScreen(
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onRecipeClick: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val recipes by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(recipes) { _, recipe ->
                RecipeListItem(recipe = recipe, onCardClick = { onRecipeClick(recipe.id) })
            }
        }
    }
}


@Composable
fun CreateRecipeScreen(
    addRecipeViewModel: AddRecipeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    val instructions = remember { mutableStateListOf<InstructionDraft>() }

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create Recipe", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        MealTypeDropdown(mealType, { mealType = it }, Modifier.fillMaxWidth())
        CategoriesMultiDropdown(categories, { categories = it }, Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(totalTime, { totalTime = it }, label = { Text("Total time (min)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(difficulty, { difficulty = it }, label = { Text("Difficulty (1-5)") }, modifier = Modifier.weight(1f))
        }

        IngredientsEditor(
            ingredients = ingredients,
            onAdd = { ingredients.add(it) },
            onRemoveAt = { idx -> ingredients.removeAt(idx) },
            modifier = Modifier.fillMaxWidth()
        )

        InstructionsEditor(
            instructions = instructions,
            onAdd = { instructions.add(it) },
            onRemoveAt = { idx -> instructions.removeAt(idx) },
            modifier = Modifier.fillMaxWidth()
        )

        ImagePickerField(
            existingImagePath = null,
            pickedImageUri = pickedImageUri,
            onPick = { pickedImageUri = it },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (mealType.isBlank() || name.isBlank()) return@Button

                val total = totalTime.toIntOrNull() ?: 0
                val diff = difficulty.toIntOrNull() ?: 1
                val ingredientList = ingredients.map {
                    Ingredient(
                        recipeId = 0,
                        name = it.name,
                        amount = it.amount.ifBlank { null },
                        unit = it.unit.ifBlank { null }
                    )
                }

                val instructionList = instructions.mapIndexed { idx, s ->
                    Instruction(
                        recipeId = 0,
                        stepNumber = idx + 1,
                        text = s.text,
                        timer = s.timer.toIntOrNull() ?: 0
                    )
                }
                scope.launch {
                    val savedPath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }

                    addRecipeViewModel.addRecipe(
                        name = name,
                        mealType = mealType,
                        categories = categories.joinToString(","),
                        imagePath = savedPath,
                        ingredients = ingredientList,
                        instructions = instructionList,
                        totalTime = total,
                        difficulty = diff
                    )
                    onFinished()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }

        OutlinedButton(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
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
            Text("${recipe.mealType} • ${recipe.totalTime} min • Difficulty ${recipe.difficulty}")

            Spacer(Modifier.height(10.dp))
            CategoriesChips(categoriesCsv = recipe.categories)

        }
    }
}

@Composable
fun RecipeDetailView(
    recipeDetailViewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController? = null,
    onEditClick: (Int) -> Unit
) {
    val state by recipeDetailViewModel.recipeDetailUiState.collectAsStateWithLifecycle()

    RecipeDetails(
        recipe = state.recipe,
        onBackClick = { navController?.popBackStack() },
        onEditClick = { onEditClick(state.recipe.id) }
    )
}


@Composable
fun RecipeDetails(
    recipe: Recipe,
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(recipe.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("${recipe.mealType} • ${recipe.totalTime} min • Difficulty ${recipe.difficulty}")

            Spacer(Modifier.height(10.dp))
            CategoriesChips(categoriesCsv = recipe.categories)

            Spacer(Modifier.height(16.dp))

            if (!recipe.imagePath.isNullOrBlank()) {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = "Recipe image",
                        modifier = Modifier.fillMaxWidth().height(220.dp),
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

            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) { Text("Go Back") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) { Text("Edit Recipe") }
        }
    }
}

@Composable
    fun EditRecipeScreen(
viewModel: RecipeEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
onSaved: () -> Unit,
onDeleted: () -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    val instructions = remember { mutableStateListOf<InstructionDraft>() }

    LaunchedEffect(ui.recipeId) {
        ingredients.clear()
        instructions.clear()

        // from existing VM text into drafts
        ui.ingredientsText
            .lines().map { it.trim() }.filter { it.isNotBlank() }
            .forEach { line ->
                val parts = line.split(" ").filter { it.isNotBlank() }
                if (parts.size >= 3) {
                    ingredients.add(
                        IngredientDraft(amount = parts[0], unit = parts[1], name = parts.drop(2).joinToString(" "))
                    )
                } else {
                    ingredients.add(IngredientDraft(name = line))
                }
            }

        ui.instructionsText
            .lines().map { it.trim() }.filter { it.isNotBlank() }
            .forEach { line ->
                instructions.add(InstructionDraft(text = line))
            }
    }

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Recipe", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(ui.name, viewModel::updateName, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        MealTypeDropdown(ui.mealType, viewModel::updateMealType, Modifier.fillMaxWidth())
        CategoriesMultiDropdown(ui.categories, viewModel::updateCategories, Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(ui.totalTime, viewModel::updateTotalTime, label = { Text("Total time (min)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(ui.difficulty, viewModel::updateDifficulty, label = { Text("Difficulty (1-5)") }, modifier = Modifier.weight(1f))
        }

        IngredientsEditor(
            ingredients = ingredients,
            onAdd = { ingredients.add(it) },
            onRemoveAt = { idx -> ingredients.removeAt(idx) },
            modifier = Modifier.fillMaxWidth()
        )

        InstructionsEditor(
            instructions = instructions,
            onAdd = { instructions.add(it) },
            onRemoveAt = { idx -> instructions.removeAt(idx) },
            modifier = Modifier.fillMaxWidth()
        )


        ImagePickerField(
            existingImagePath = ui.imagePath,
            pickedImageUri = pickedImageUri,
            onPick = { pickedImageUri = it },
            onRemoveExisting = { viewModel.updateImagePath(null) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    val ingredientsText = ingredients.joinToString("\n") { ing ->
                        listOfNotNull(
                            ing.amount.takeIf { it.isNotBlank() },
                            ing.unit.takeIf { it.isNotBlank() },
                            ing.name.takeIf { it.isNotBlank() }
                        ).joinToString(" ")
                    }

                    val instructionsText = instructions.joinToString("\n") { it.text }
                    viewModel.updateIngredientsText(ingredientsText)
                    viewModel.updateInstructionsText(instructionsText)


                    val newPath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }
                    if (newPath != null) viewModel.updateImagePath(newPath)

                    viewModel.save(onSaved)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Changes") }

        Button(
            onClick = {
                viewModel.delete(onDeleted)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) { Text("Delete Recipe") }

        OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
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
