package at.uastw.fishdiary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.layout.ContentScale
import androidx.activity.result.PickVisualMediaRequest
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Add

import java.util.Locale


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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
private fun EditIngredientDialog(
    initial: IngredientDraft,
    onDismiss: () -> Unit,
    onSave: (IngredientDraft) -> Unit
) {
    var amount by remember { mutableStateOf(initial.amount) }
    var unit by remember { mutableStateOf(initial.unit) }
    var name by remember { mutableStateOf(initial.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) return@Button
                onSave(IngredientDraft(amount.trim(), unit.trim(), name.trim()))
            }) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditInstructionDialog(
    initial: InstructionDraft,
    onDismiss: () -> Unit,
    onSave: (InstructionDraft) -> Unit
) {
    var text by remember { mutableStateOf(initial.text) }
    var timer by remember { mutableStateOf(initial.timer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Step") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Step") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = timer,
                    onValueChange = { timer = it },
                    label = { Text("Timer (min, optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (text.isBlank()) return@Button
                onSave(InstructionDraft(text.trim(), timer.trim()))
            }) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
private fun IngredientsEditor(
    ingredients: List<IngredientDraft>,
    onAdd: (IngredientDraft) -> Unit,
    onRemoveAt: (Int) -> Unit,
    onEditAt: (Int) -> Unit,
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
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditAt(idx) }
                ) {
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
    onEditAt: (Int) -> Unit,
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
            label = { Text("Timer (minute, optional)") },
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
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditAt(idx) }
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}. ${step.text}" +
                                    (step.timer.toIntOrNull()?.let { if (it > 0) "  (${it} min)" else "" } ?: ""),
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
        navController: NavHostController = rememberNavController(),
        timerViewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)
    ) {
        val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
        val displayMinutes = if (timerState.isRunning) timerState.remainingTime / 60 else timerState.minutes
        val displaySeconds = if (timerState.isRunning) timerState.remainingTime % 60 else timerState.seconds
        val context = LocalContext.current

        // Store MediaPlayer reference
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        // Play sound when dialog shows
        LaunchedEffect(timerState.showCompletionDialog) {
            if (timerState.showCompletionDialog) {
                try {
                    // Release previous player if exists
                    mediaPlayer?.release()

                    // Try multiple possible paths and filenames
                    val possiblePaths = listOf(
                        "themes/sounds/sound.mp3",
                        "sounds/sound.mp3"
                    )

                    var player: MediaPlayer?
                    for (path in possiblePaths) {
                        try {
                            val assetFileDescriptor = context.assets.openFd(path)
                            player = MediaPlayer().apply {
                                setDataSource(
                                    assetFileDescriptor.fileDescriptor,
                                    assetFileDescriptor.startOffset,
                                    assetFileDescriptor.length
                                )
                                prepare()
                                isLooping = true
                            }
                            assetFileDescriptor.close()
                            mediaPlayer = player
                            player.start()
                            break // Successfully loaded and playing
                        } catch (_: Exception) {
                            // Try next path
                            continue
                        }
                    }
                } catch (_: Exception) {
                    // Handle error - sound file might not be found
                    mediaPlayer = null
                }
            } else {
                // Stop and release when dialog is dismissed
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
            }
        }

        // Cleanup MediaPlayer when composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
            }
        }

        // Show completion dialog when timer reaches 0
        if (timerState.showCompletionDialog) {
            AlertDialog(
                onDismissRequest = {
                    mediaPlayer?.apply {
                        if (isPlaying) {
                            stop()
                        }
                        release()
                    }
                    mediaPlayer = null
                    timerViewModel.dismissCompletionDialog()
                },
                title = { Text("Time is up!") },
                confirmButton = {
                    Button(onClick = {
                        mediaPlayer?.apply {
                            if (isPlaying) {
                                stop()
                            }
                            release()
                        }
                        mediaPlayer = null
                        timerViewModel.dismissCompletionDialog()
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Timer bar at the top when running
                if (timerState.isRunning) {
                    TimerTopBar(
                        minutes = displayMinutes,
                        seconds = displaySeconds,
                        onPause = { timerViewModel.pauseTimer() },
                        onClear = { timerViewModel.clearTimer() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                NavHost(
                    navController = navController,
                    modifier = Modifier.weight(1f),
                    startDestination = Routes.List.route
                ) {

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
                    onEditClick = { id -> navController.navigate("edit/$id") },
                    timerViewModel = timerViewModel
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
    modifier: Modifier = Modifier,
    label: String = "Image (optional)",
    existingImagePath: String?,
    pickedImageUri: Uri?,
    onPick: (Uri?) -> Unit,
    onRemoveExisting: (() -> Unit)? = null,

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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

// Woopper Style Homepage
@Composable
fun RecipesHomeScreen(
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onRecipeClick: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val recipes by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()

    // Group recipes by meal type
    val recipesByMealType = recipes.groupBy { it.mealType }

    Box(
        modifier = Modifier
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
            onClick = onAddClick,
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

@OptIn(ExperimentalMaterial3Api::class)
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
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
fun CreateRecipeScreen(
    addRecipeViewModel: AddRecipeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFinished: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var totalTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    val instructions = remember { mutableStateListOf<InstructionDraft>() }
    var notes by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }

    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var editingInstructionIndex by remember { mutableStateOf<Int?>(null) }

    editingIngredientIndex
        ?.takeIf { it in ingredients.indices }
        ?.let { idx ->
            EditIngredientDialog(
                initial = ingredients[idx],
                onDismiss = { editingIngredientIndex = null },
                onSave = { updated ->
                    ingredients[idx] = updated
                    editingIngredientIndex = null
                }
            )
        }

    editingInstructionIndex
        ?.takeIf { it in instructions.indices }
        ?.let { idx ->
            EditInstructionDialog(
                initial = instructions[idx],
                onDismiss = { editingInstructionIndex = null },
                onSave = { updated ->
                    instructions[idx] = updated
                    editingInstructionIndex = null
                }
            )
        }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
            OutlinedTextField(servingSize, { servingSize = it }, label = { Text("Serving Size") }, modifier = Modifier.weight(1f))
        }

        IngredientsEditor(
            ingredients = ingredients,
            onAdd = { ingredients.add(it) },
            onRemoveAt = { idx -> ingredients.removeAt(idx) },
            onEditAt = { idx -> editingIngredientIndex = idx },
            modifier = Modifier.fillMaxWidth()
        )

        InstructionsEditor(
            instructions = instructions,
            onAdd = { instructions.add(it) },
            onRemoveAt = { idx -> instructions.removeAt(idx) },
            onEditAt = { idx -> editingInstructionIndex = idx },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth())

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
                val serveSize = servingSize.toIntOrNull() ?: 1

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
                        notes = notes,
                        totalTime = total,
                        difficulty = diff,
                        servingSize = serveSize,
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
    onEditClick: (Int) -> Unit,
    timerViewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by recipeDetailViewModel.recipeDetailUiState.collectAsStateWithLifecycle()

    RecipeDetails(
        recipe = state.recipe,
        onBackClick = { navController?.popBackStack() },
        onEditClick = { onEditClick(state.recipe.id) },
        timerViewModel = timerViewModel
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerComponent(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Timer Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minutes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        if (minutes < 99) {
                            onMinutesChange(minutes + 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase minutes",
                        tint = Color.White
                    )
                }

                Text(
                    text = String.format(Locale.getDefault(), "%02d", minutes),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = {
                        if (minutes > 0) {
                            onMinutesChange(minutes - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease minutes",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = ":",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Seconds
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        if (seconds < 50) {
                            onSecondsChange(seconds + 10)
                        } else {
                            onSecondsChange(0)
                            if (minutes < 99) onMinutesChange(minutes + 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase seconds",
                        tint = Color.White
                    )
                }

                Text(
                    text = String.format(Locale.getDefault(), "%02d", seconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = {
                        if (seconds > 0) {
                            onSecondsChange(seconds - 10)
                        } else {
                            onSecondsChange(50)
                            if (minutes > 0) onMinutesChange(minutes - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease seconds",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Play Button
            FloatingActionButton(
                onClick = {
                    if (minutes > 0 || seconds > 0) {
                        onStart()
                    }
                },
                containerColor = Color(0xFF4DD0E1), // Light blue/teal
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Reset button
        if (minutes > 0 || seconds > 0) {
            TextButton(onClick = {
                onMinutesChange(0)
                onSecondsChange(0)
            }) {
                Text("Reset", color = Color.White)
            }
        }
    }
}

@Composable
fun TimerTopBar(
    minutes: Int,
    seconds: Int,
    onPause: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFFFB5A7), // Light coral/peach color
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer: ${String.format(Locale.getDefault(), "%02d", minutes)}:${String.format(Locale.getDefault(), "%02d", seconds)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause button
                IconButton(onClick = onPause) {
                    // Custom pause icon (two rectangles)
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(16.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(16.dp)
                                    .background(Color.White, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }

                // Clear/Stop button (X icon)
                IconButton(onClick = onClear) {
                    // Custom X icon (clear/stop)
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ServingsAdjuster(
    baseServings: Int,
    desiredServings: Int,
    onDesiredChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    var input by remember(desiredServings) { mutableStateOf(desiredServings.toString()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Servings:", style = MaterialTheme.typography.bodyLarge)

        OutlinedButton(
            onClick = { open = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = desiredServings.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (desiredServings != baseServings) {
            Text(
                text = "base $baseServings",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = { onDesiredChange(baseServings) }) { Text("Reset") }
        } else {
            Text(
                text = "base",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Adjust servings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Base recipe: $baseServings")
                    OutlinedTextField(
                        value = input,
                        onValueChange = { v -> input = v.filter { it.isDigit() }.take(3) },
                        label = { Text("Servings") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val v = input.toIntOrNull()?.coerceIn(1, 999) ?: baseServings
                    onDesiredChange(v)
                    open = false
                }) { Text("Apply") }
            },
            dismissButton = {
                OutlinedButton(onClick = { open = false }) { Text("Cancel") }
            }
        )
    }
}

private fun parseAmountToDouble(amount: String): Double? {
    val a = amount.trim().lowercase()

    val mixed = Regex("""^(\d+)\s+(\d+)\s*/\s*(\d+)$""").matchEntire(a)
    if (mixed != null) {
        val whole = mixed.groupValues[1].toDouble()
        val num = mixed.groupValues[2].toDouble()
        val den = mixed.groupValues[3].toDouble()
        if (den != 0.0) return whole + (num / den)
    }

    val frac = Regex("""^(\d+)\s*/\s*(\d+)$""").matchEntire(a)
    if (frac != null) {
        val num = frac.groupValues[1].toDouble()
        val den = frac.groupValues[2].toDouble()
        if (den != 0.0) return num / den
    }

    return a.replace(",", ".").toDoubleOrNull()
}

private fun formatAmount(value: Double): String {
    val rounded = kotlin.math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else rounded.toString().trimEnd('0').trimEnd('.')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetails(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    timerViewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showTimerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()

    val baseServings = recipe.servingSize.coerceAtLeast(1)
    var desiredServings by remember(recipe.id) { mutableStateOf(baseServings) }
    val scaleFactor = desiredServings.toDouble() / baseServings.toDouble()

    OutlinedCard(
        modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(recipe.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("${recipe.mealType} • ${recipe.totalTime} min • Difficulty ${recipe.difficulty}")

            Spacer(Modifier.height(10.dp))
            CategoriesChips(categoriesCsv = recipe.categories)

            Spacer(Modifier.height(12.dp))

            ServingsAdjuster(
                baseServings = baseServings,
                desiredServings = desiredServings,
                onDesiredChange = { desiredServings = it },
                modifier = Modifier.fillMaxWidth()
            )

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
                val baseAmountStr = ing.amount?.trim().orEmpty()
                val baseAmountNum = baseAmountStr.takeIf { it.isNotBlank() }?.let(::parseAmountToDouble)

                val scaledAmountStr = when {
                    baseAmountNum != null -> formatAmount(baseAmountNum * scaleFactor)
                    else -> baseAmountStr
                }

                val amountUnit = listOfNotNull(
                    scaledAmountStr.takeIf { it.isNotBlank() },
                    ing.unit?.takeIf { it.isNotBlank() }
                ).joinToString(" ")

                Text("• ${amountUnit.ifBlank { "" }} ${ing.name}".trim())
            }

            Spacer(Modifier.height(12.dp))

            Text("Instructions:", style = MaterialTheme.typography.titleMedium)
            recipe.instructions
                .sortedBy { it.stepNumber }
                .forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${step.stepNumber}. ${step.text}",
                            modifier = Modifier.weight(1f)
                        )

                        if (step.timer > 0) {
                            Button(
                                onClick = {
                                    timerViewModel.setMinutes(step.timer)
                                    timerViewModel.setSeconds(0)
                                    showTimerSheet = true
                                }
                            ) {
                                Text("${step.timer} min")
                            }
                        }
                    }
                }

            Spacer(Modifier.height(12.dp))

            Text("Notes:", style = MaterialTheme.typography.titleMedium)
            Text(recipe.notes)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { showTimerSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Set Timer") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Go Back") }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Recipe") }
        }
    }

    if (showTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimerSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFB5A7),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            TimerComponent(
                minutes = timerState.minutes,
                seconds = timerState.seconds,
                onMinutesChange = { timerViewModel.setMinutes(it) },
                onSecondsChange = { timerViewModel.setSeconds(it) },
                onStart = {
                    timerViewModel.startTimer()
                    showTimerSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            )
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

            var hydrated by remember(ui.recipeId) { mutableStateOf(false) }

            LaunchedEffect(ui.loaded, ui.recipeId) {
                if (ui.loaded && !hydrated) {
                    ingredients.clear()
                    ingredients.addAll(ui.ingredients)

                    instructions.clear()
                    instructions.addAll(ui.instructions)

                    hydrated = true
                }
            }

            var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }
            var editingInstructionIndex by remember { mutableStateOf<Int?>(null) }

            editingIngredientIndex
                ?.takeIf { it in ingredients.indices }
                ?.let { idx ->
                    EditIngredientDialog(
                        initial = ingredients[idx],
                        onDismiss = { editingIngredientIndex = null },
                        onSave = { updated ->
                            ingredients[idx] = updated
                            editingIngredientIndex = null
                        }
                    )
                }

            editingInstructionIndex
                ?.takeIf { it in instructions.indices }
                ?.let { idx ->
                    EditInstructionDialog(
                        initial = instructions[idx],
                        onDismiss = { editingInstructionIndex = null },
                        onSave = { updated ->
                            instructions[idx] = updated
                            editingInstructionIndex = null
                        }
                    )
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

                OutlinedTextField(
                    ui.name,
                    viewModel::updateName,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                MealTypeDropdown(ui.mealType, viewModel::updateMealType, Modifier.fillMaxWidth())
                CategoriesMultiDropdown(
                    ui.categories,
                    viewModel::updateCategories,
                    Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        ui.totalTime,
                        viewModel::updateTotalTime,
                        label = { Text("Total time (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        ui.difficulty,
                        viewModel::updateDifficulty,
                        label = { Text("Difficulty (1-5)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        ui.servingSize,
                        viewModel::updateServingSize,
                        label = { Text("Serving Size") },
                        modifier = Modifier.weight(1f)
                    )
                }

                IngredientsEditor(
                    ingredients = ingredients,
                    onAdd = { ingredients.add(it) },
                    onRemoveAt = { idx -> ingredients.removeAt(idx) },
                    onEditAt = { idx -> editingIngredientIndex = idx },
                    modifier = Modifier.fillMaxWidth()
                )

                InstructionsEditor(
                    instructions = instructions,
                    onAdd = { instructions.add(it) },
                    onRemoveAt = { idx -> instructions.removeAt(idx) },
                    onEditAt = { idx -> editingInstructionIndex = idx },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    ui.notes,
                    viewModel::updateNotes,
                    label = { Text("Notes") },
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
                            val newPath =
                                pickedImageUri?.let { copyImageToInternalStorage(context, it) }
                            if (newPath != null) viewModel.updateImagePath(newPath)

                            viewModel.save(
                                ingredientDrafts = ingredients.toList(),
                                instructionDrafts = instructions.toList(),
                                onFinished = onSaved
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Changes") }

                Button(
                    onClick = { viewModel.delete(onDeleted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete Recipe") }

                OutlinedButton(
                    onClick = onSaved,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            }
        }


