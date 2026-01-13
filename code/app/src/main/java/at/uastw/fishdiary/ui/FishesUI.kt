package at.uastw.fishdiary.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.uastw.fishdiary.data.Recipe
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items


private val categoryArr = listOf(
    "Freshwater",
    "Saltwater",
    "Cool",
    "Squid",
    "Legendary",
    "Tropical",
    "Endangered",
    "Extinct",
    "Would make a good pet",
    "Turtle",
    "Not a turtle",
    "Looks like a turtle",
    "Crab that's also an alligator (weapon)",
    "Can be eaten"
)

enum class Routes(val route: String) {
    Catalogue("catalogue"),
    Create("create"),
    Detail("detail/{fishId}"),
    Edit("edit/{fishId}")
}

@Composable
fun FishDiaryApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController,
        modifier = modifier,
        startDestination = Routes.Catalogue.route
    ) {
        composable(Routes.Catalogue.route) {
            CatalogueView(
                navController = navController,
                onFishClick = { fishId -> navController.navigate("detail/$fishId") },
                onCreateClick = { navController.navigate(Routes.Create.route) }
            )
        }

        composable(Routes.Create.route) {
            CreateFishView(
                navController = navController
            )
        }

        composable(
            Routes.Detail.route,
            listOf(navArgument("fishId") { type = NavType.IntType })
        ) {
            FishDetailView(
                navController = navController,
                onEdit = { fishId -> navController.navigate("edit/$fishId") }
            )
        }

        composable(
            Routes.Edit.route,
            listOf(navArgument("fishId") { type = NavType.IntType })
        ) {
            FishEditView(navController = navController)
        }
    }
}

@Composable
private fun FishDiaryHeader(
    showBack: Boolean,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "wopper",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val outFile = File(context.filesDir, "fish_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { output ->
                input.use { it.copyTo(output) }
            }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

private fun categoriesStringToSet(value: String): Set<String> =
    value.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

private fun categoriesSetToString(value: Set<String>): String =
    value.joinToString(", ")

private fun fishMatchesAnySelectedCategory(recipe: Recipe, selected: Set<String>): Boolean {
    if (selected.isEmpty()) return true
    val fishCats = categoriesStringToSet(recipe.categories)
    return fishCats.any { it in selected }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesMultiSelectDropdown(
    label: String,
    allOptions: List<String>,
    selected: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = remember(selected) {
        if (selected.isEmpty()) "All" else selected.joinToString(", ")
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = option in selected,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(option)
                        }
                    },
                    onClick = {
                        val newSet = selected.toMutableSet()
                        if (option in newSet) newSet.remove(option) else newSet.add(option)
                        onSelectedChange(newSet)
                    }
                )
            }
            Divider()
            DropdownMenuItem(
                text = { Text("Clear") },
                onClick = { onSelectedChange(emptySet()) }
            )
        }
    }
}

@Composable
fun FishGridItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            val path = recipe.imagePath

            if (!path.isNullOrBlank()) {
                AsyncImage(
                    model = File(path),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun CatalogueView(
    navController: NavHostController,
    fishesViewModel: FishesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFishClick: (Int) -> Unit,
    onCreateClick: () -> Unit
) {
    val fishes by fishesViewModel.fishesUiState.collectAsStateWithLifecycle()
    var filterCategories by remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(fishes, filterCategories) {
        fishes.filter { fishMatchesAnySelectedCategory(it, filterCategories) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FishDiaryHeader(showBack = false, onBack = {})

        Column(Modifier.padding(16.dp)) {
            CategoriesMultiSelectDropdown(
                label = "Filter by categories",
                allOptions = categoryArr,
                selected = filterCategories,
                onSelectedChange = { filterCategories = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth()) {
                Text("Create Fish Entry")
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { fish ->
                    FishGridItem(
                        recipe = fish,
                        onClick = { onFishClick(fish.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun CreateFishView(
    navController: NavHostController,
    fishesViewModel: FishesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasSeen by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        FishDiaryHeader(
            showBack = true,
            onBack = { navController.popBackStack() }
        )

        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            CategoriesMultiSelectDropdown(
                label = "Categories",
                allOptions = categoryArr,
                selected = selectedCategories,
                onSelectedChange = { selectedCategories = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("I have seen this fish:")
                Spacer(Modifier.size(8.dp))
                Checkbox(checked = hasSeen, onCheckedChange = { hasSeen = it })
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { picker.launch("image/*") }) {
                    Text(if (pickedImageUri == null) "Pick image" else "Change image")
                }

                if (pickedImageUri != null) {
                    AsyncImage(
                        model = pickedImageUri,
                        contentDescription = "Picked fish image",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    scope.launch {
                        val imagePath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }
                        fishesViewModel.addFish(
                            name = name,
                            description = description,
                            categories = categoriesSetToString(selectedCategories),
                            hasSeen = hasSeen,
                            imagePath = imagePath
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Fish")
            }
        }
    }
}

@Composable
fun FishListItem(
    recipe: Recipe,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val path = recipe.imagePath
            if (!path.isNullOrBlank()) {
                AsyncImage(
                    model = File(path),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.size(12.dp))
            }
            Text(recipe.name, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun FishDetailView(
    fishDetailViewModel: FishDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController,
    onEdit: (Int) -> Unit
) {
    val state by fishDetailViewModel.fishDetailUiState.collectAsStateWithLifecycle()

    FishDetails(
        recipe = state.recipe,
        onBackClick = { navController.popBackStack() },
        onEditClick = { onEdit(state.recipe.id) },
        onDeleteClick = {
            fishDetailViewModel.onDeleteFish {
                navController.navigate(Routes.Catalogue.route) {
                    popUpTo(Routes.Catalogue.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    )
}

@Composable
fun FishDetails(
    recipe: Recipe,
    onDeleteClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        FishDiaryHeader(showBack = true, onBack = onBackClick)

        OutlinedCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(18.dp)) {

                Text(
                    text = recipe.name.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                val path = recipe.imagePath
                if (!path.isNullOrBlank()) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = recipe.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(26.dp))
                    )
                    Spacer(Modifier.height(14.dp))
                }

                Text("Description:", style = MaterialTheme.typography.titleMedium)
                Text(recipe.description)

                Spacer(Modifier.height(12.dp))

                Text("Categories:", style = MaterialTheme.typography.titleMedium)
                Text(recipe.categories)

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("I have seen this fish:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.size(10.dp))
                    Checkbox(checked = recipe.hasSeen, onCheckedChange = null)
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit")
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete Fish")
                }
            }
        }
    }
}

@Composable
fun FishEditView(
    navController: NavHostController,
    viewModel: FishEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategories by remember(state.recipe.categories) {
        mutableStateOf(categoriesStringToSet(state.recipe.categories))
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        FishDiaryHeader(showBack = true, onBack = { navController.popBackStack() })

        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = state.recipe.name,
                onValueChange = viewModel::updateName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.recipe.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            CategoriesMultiSelectDropdown(
                label = "Categories",
                allOptions = categoryArr,
                selected = selectedCategories,
                onSelectedChange = {
                    selectedCategories = it
                    viewModel.updateCategories(categoriesSetToString(it))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("I have seen this fish:")
                Spacer(Modifier.size(8.dp))
                Checkbox(checked = state.recipe.hasSeen, onCheckedChange = viewModel::updateHasSeen)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { picker.launch("image/*") }) {
                    Text("Pick image")
                }

                val previewModel = pickedImageUri ?: state.recipe.imagePath?.let { File(it) }

                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Fish image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        val newPath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }
                        if (newPath != null) viewModel.updateImagePath(newPath)
                        viewModel.save {
                            navController.navigate(Routes.Catalogue.route) {
                                popUpTo(Routes.Catalogue.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
