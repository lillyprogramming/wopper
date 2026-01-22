package at.uastw.fishdiary.ui

import android.media.MediaPlayer
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically


private val Peach = Color(0xFFF7AF9D)
private val Blue = Color(0xFFB0D0D3)
private val Pink = Color(0xFFC08497)
private val LightPink = Color(0xFFFFCAD4)

private fun MediaPlayer.safeStopAndRelease() {
    try { if (isPlaying) stop() } catch (_: Exception) {}
    try { release() } catch (_: Exception) {}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DigitsOnlyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    maxDigits: Int = 4,
    min: Int? = null,
    max: Int? = null,
    isError: Boolean = false,
    supportingText: (@Composable (() -> Unit))? = null,
    colors: androidx.compose.material3.TextFieldColors = woopperTextFieldColors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            val filtered = v.filter { it.isDigit() }.take(maxDigits)
            if (filtered.isBlank()) onValueChange("")
            else {
                val n0 = filtered.toIntOrNull()
                if (n0 == null) onValueChange("")
                else {
                    val n1 = min?.let { kotlin.math.max(it, n0) } ?: n0
                    val n2 = max?.let { kotlin.math.min(it, n1) } ?: n1
                    onValueChange(n2.toString())
                }
            }
        },
        label = label,
        modifier = modifier,
        isError = isError,
        supportingText = supportingText,
        colors = colors
    )
}
enum class Routes(val route: String) {
    List("list"),
    Create("create"),
    Detail("detail/{recipeId}"),
    Edit("edit/{recipeId}")
}

data class IngredientDraft(val amount: String = "", val unit: String = "", val name: String = "")
data class InstructionDraft(val text: String = "", val timer: String = "")

private val unitArr = listOf("g", "kg", "ml", "l", "tbsp", "tsp", "cup", "pcs", "pinch")

private val mealTypeArr = listOf(
    "Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Drinks", "Salads", "Side Dishes", "Soups", "Snacks", "Sauces"
)

private val categoriesArr = listOf(
    "Vegan", "Vegetarian", "Spicy", "Sour", "Sweet",
    "Pasta", "Whole-grain", "Chicken", "Beef", "Pork", "Seafood",
    "Nuts", "Lactose", "Gluten", "Nut-free", "Lactose-free", "Gluten-free", "Fast-friendly"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    unit: String,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.TextFieldColors = woopperTextFieldColors()
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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = colors,
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            unitArr.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onUnitChange(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun WoopperInputCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Pink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditIngredientDialog(
    initial: IngredientDraft,
    onDismiss: () -> Unit,
    onSave: (IngredientDraft) -> Unit
) {
    var amount by remember { mutableStateOf(initial.amount) }
    var unit by remember { mutableStateOf(initial.unit) }
    var name by remember { mutableStateOf(initial.name) }
    val nameErr = remember(name) { name.trim().isBlank() }

    val dialogFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Pink,
        unfocusedBorderColor = Pink.copy(alpha = 0.85f),
        cursorColor = Pink,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black.copy(alpha = 0.9f),
        errorBorderColor = Pink,
        errorLabelColor = Color.Black,
        errorCursorColor = Pink,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        errorContainerColor = Color.White,
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Peach,
        title = { Text("Edit Ingredient", fontWeight = FontWeight.Black, color = Color(0xFF111827)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameErr,
                    supportingText = { if (nameErr) Text("Required", color = Pink) },
                    colors = dialogFieldColors,
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { v ->
                            amount = v.filter { it.isDigit() || it == ' ' || it == '/' || it == '.' || it == ',' }.take(20)
                        },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        colors = dialogFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    UnitDropdown(
                        unit = unit,
                        onUnitChange = { unit = it },
                        modifier = Modifier.weight(1f),
                        colors = dialogFieldColors
                    )
                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isBlank()) return@Button
                    onSave(IngredientDraft(amount.trim(), unit.trim(), name.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Pink),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink)
            ) { Text("Cancel", fontWeight = FontWeight.Bold) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInstructionDialog(
    initial: InstructionDraft,
    onDismiss: () -> Unit,
    onSave: (InstructionDraft) -> Unit
) {
    var text by remember { mutableStateOf(initial.text) }
    var timer by remember { mutableStateOf(initial.timer) }
    val textErr = remember(text) { text.trim().isBlank() }

    val dialogFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Pink,
        unfocusedBorderColor = Pink.copy(alpha = 0.85f),
        cursorColor = Pink,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black.copy(alpha = 0.9f),
        errorBorderColor = Pink,
        errorLabelColor = Color.Black,
        errorCursorColor = Pink,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        errorContainerColor = Color.White,
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Peach,
        title = { Text("Edit Step", fontWeight = FontWeight.Black, color = Color(0xFF111827)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Step") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = textErr,
                    supportingText = { if (textErr) Text("Required", color = Pink) },
                    colors = dialogFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )

                DigitsOnlyField(
                    value = timer,
                    onValueChange = { timer = it },
                    label = { Text("Minutes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxDigits = 3,
                    min = 0,
                    colors = dialogFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.trim().isBlank()) return@Button
                    onSave(InstructionDraft(text.trim(), timer.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Pink),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink)
            ) { Text("Cancel", fontWeight = FontWeight.Bold) }
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
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ingredient") },
            modifier = Modifier.fillMaxWidth(),
            colors = woopperTextFieldColors(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { v ->
                    amount = v.filter { it.isDigit() || it == ' ' || it == '/' || it == '.' || it == ',' }.take(20)
                },
                label = { Text("Amount") },
                modifier = Modifier.weight(1f),
                colors = woopperTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            UnitDropdown(unit = unit, onUnitChange = { unit = it }, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(10.dp))


        Button(
            onClick = {
                if (name.isBlank()) return@Button
                onAdd(IngredientDraft(amount = amount.trim(), unit = unit.trim(), name = name.trim()))
                amount = ""; unit = ""; name = ""
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Add Ingredient", fontWeight = FontWeight.Bold) }

        if (ingredients.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ingredients.forEachIndexed { idx, ing ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditAt(idx) },
                    border = BorderStroke(1.dp, Pink),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFE8F4F5)),
                    shape = RoundedCornerShape(16.dp)
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
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { onRemoveAt(idx) },
                            border = BorderStroke(1.dp, Pink),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Remove", fontWeight = FontWeight.Bold) }
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
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Step") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            colors = woopperTextFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(10.dp))

        DigitsOnlyField(
            value = timer,
            onValueChange = { timer = it },
            label = { Text("Timer (minute, optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxDigits = 3,
            min = 0,
            colors = woopperTextFieldColors()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (text.isBlank()) return@Button
                onAdd(InstructionDraft(text = text.trim(), timer = timer.trim()))
                text = ""; timer = ""
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Add Step", fontWeight = FontWeight.Bold) }

        if (instructions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            instructions.forEachIndexed { idx, step ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditAt(idx) },
                    border = BorderStroke(1.dp, Pink),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFE8F4F5)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${idx + 1}. ${step.text}" +
                                    (step.timer.toIntOrNull()?.let { if (it > 0) "  (${it} min)" else "" } ?: ""),
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { onRemoveAt(idx) },
                            border = BorderStroke(1.dp, Pink),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Remove", fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeDropdown(mealType: String, onMealTypeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = mealType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Meal type*") },
            placeholder = { Text("Select") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = woopperTextFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            mealTypeArr.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onMealTypeChange(option); expanded = false }
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
    val selectedText = remember(selected) { if (selected.isEmpty()) "" else selected.joinToString(", ") }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categories") },
            placeholder = { Text("Select categories") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = woopperTextFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categoriesArr.forEach { option ->
                val isChecked = selected.contains(option)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    },
                    onClick = {
                        val newSelected = if (isChecked) selected - option else selected + option
                        onSelectedChange(newSelected)
                    }
                )
            }
        }
    }
}

@Composable
fun FishDiaryApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    timerViewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val displayMinutes = if (timerState.remainingTime > 0) timerState.remainingTime / 60 else timerState.minutes
    val displaySeconds = if (timerState.remainingTime > 0) timerState.remainingTime % 60 else timerState.seconds
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
                timerViewModel.dismissCompletionDialogAndClear()
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
                    timerViewModel.dismissCompletionDialogAndClear()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Timer bar at the top when running or paused
            if (timerState.remainingTime > 0) {
                TimerTopBar(
                    minutes = displayMinutes,
                    seconds = displaySeconds,
                    isRunning = timerState.isRunning,
                    onTogglePause = { timerViewModel.togglePauseResume() },
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
    csv.split(",").map { it.trim() }.filter { it.isNotBlank() }

private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val dir = File(context.filesDir, "recipe_images").apply { mkdirs() }
            val outFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { output -> input.use { it.copyTo(output) } }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

@Composable
fun ImagePickerField(
    modifier: Modifier = Modifier,
    existingImagePath: String?,
    pickedImageUri: Uri?,
    onPick: (Uri?) -> Unit,
    onRemoveExisting: (() -> Unit)? = null
) {
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onPick(uri) }

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (pickedImageUri == null) "Pick Image" else "Change", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                onClick = {
                    onPick(null)
                    onRemoveExisting?.invoke()
                },
                enabled = pickedImageUri != null || (existingImagePath != null && onRemoveExisting != null),
                modifier = Modifier.weight(1f).height(46.dp),
                border = BorderStroke(1.dp, Pink),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Remove", fontWeight = FontWeight.Bold) }
        }

        val previewModel = when {
            pickedImageUri != null -> pickedImageUri
            !existingImagePath.isNullOrBlank() -> File(existingImagePath)
            else -> null
        }

        if (previewModel != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedCard(
                Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Pink),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CategoriesChips(categoriesCsv: String, modifier: Modifier = Modifier, title: String = "Categories:") {
    val categoriesList = remember(categoriesCsv) { parseCategoriesCsv(categoriesCsv) }
    if (categoriesList.isEmpty()) return

    Column(modifier) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Color.Black)
        Spacer(Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            categoriesList.forEach { cat ->
                AssistChip(
                    onClick = {},
                    label = { Text(cat) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Peach, labelColor = Color.Black),
                    border = BorderStroke(1.dp, Pink)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun woopperTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Pink,
    unfocusedBorderColor = Pink.copy(alpha = 0.85f),
    cursorColor = Pink,
    focusedLabelColor = Pink,
    unfocusedLabelColor = Pink.copy(alpha = 0.9f),
    errorBorderColor = Pink,
    errorLabelColor = Pink,
    errorCursorColor = Pink,
    focusedContainerColor = LightPink.copy(alpha = 0.35f),
    unfocusedContainerColor = LightPink.copy(alpha = 0.25f),
    errorContainerColor = LightPink.copy(alpha = 0.25f)
)
@Composable
private fun WoopperPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
    ) { Text(text, fontWeight = FontWeight.Medium) }
}

@Composable
private fun WoopperSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, Pink),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink)
    ) { Text(text, fontWeight = FontWeight.Medium) }
}

@Composable
fun RecipesHomeScreen(
    recipesViewModel: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onRecipeClick: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val recipes by recipesViewModel.recipesUiState.collectAsStateWithLifecycle()
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    val collapsedMealTypes = remember { mutableStateMapOf<String, Boolean>() }

    val filteredRecipes = remember(recipes, selectedCategories, searchQuery) {
        var filtered = recipes
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { recipe -> recipe.name.contains(searchQuery, ignoreCase = true) }
        }
        if (selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { recipe ->
                val recipeCategories = parseCategoriesCsv(recipe.categories).toSet()
                recipeCategories.intersect(selectedCategories).isNotEmpty()
            }
        }
        filtered
    }

    val recipesByMealType = filteredRecipes.groupBy { it.mealType }

    Box(modifier = Modifier.fillMaxSize().background(Peach)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Blue)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Text("wopper", fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Pink, modifier = Modifier.offset((-3).dp, 0.dp))
                            Text("wopper", fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Pink, modifier = Modifier.offset((3).dp, 0.dp))
                            Text("wopper", fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Pink, modifier = Modifier.offset(0.dp, (-3).dp))
                            Text("wopper", fontWeight = FontWeight.Bold, fontSize = 44.sp, color = Pink, modifier = Modifier.offset(0.dp, (3).dp))
                            Text("wopper", fontWeight = FontWeight.Bold, fontSize = 44.sp, color = LightPink)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search recipes...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Pink) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pink,
                        unfocusedBorderColor = Pink.copy(alpha = 0.85f),
                        cursorColor = Pink,
                        focusedLabelColor = Pink,
                        unfocusedLabelColor = Pink.copy(alpha = 0.9f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorContainerColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            mealTypeArr.forEach { mealType ->
                val mealTypeRecipes = recipesByMealType[mealType] ?: emptyList()
                if (mealTypeRecipes.isNotEmpty()) {
                    val collapsed = collapsedMealTypes[mealType] ?: false
                    item {
                        MealTypeSection(
                            mealType = mealType,
                            recipes = mealTypeRecipes,
                            onRecipeClick = onRecipeClick,
                            collapsed = collapsed,
                            onToggleCollapsed = {
                                collapsedMealTypes[mealType] = !(collapsedMealTypes[mealType] ?: false)
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }

        var filterMenuExpanded by remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { filterMenuExpanded = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
            containerColor = if (selectedCategories.isNotEmpty()) Blue else Pink,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
        }

        DropdownMenu(
            expanded = filterMenuExpanded,
            onDismissRequest = { filterMenuExpanded = false },
            modifier = Modifier.widthIn(max = 280.dp).heightIn(max = 400.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter by Category", fontWeight = FontWeight.Bold)
                        if (selectedCategories.isNotEmpty()) {
                            TextButton(onClick = { selectedCategories = emptySet(); filterMenuExpanded = false }) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }
                },
                onClick = {}
            )

            HorizontalDivider()

            categoriesArr.forEach { category ->
                val isSelected = selectedCategories.contains(category)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(category)
                        }
                    },
                    onClick = {
                        selectedCategories = if (isSelected) selectedCategories - category else selectedCategories + category
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Pink,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun MealTypeSection(
    mealType: String,
    recipes: List<Recipe>,
    onRecipeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    collapsed: Boolean,
    onToggleCollapsed: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleCollapsed() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    mealType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (collapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (collapsed) "Expand" else "Collapse",
                    tint = Color(0xFF111827)
                )
            }

            AnimatedVisibility(
                visible = !collapsed,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))

                    WoopperPrimaryButton(
                        text = "Pick a Random one",
                        onClick = { onRecipeClick(recipes.random().id) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    recipes.chunked(2).forEach { rowRecipes ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowRecipes.forEach { recipe ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WoopperRecipeCard(
                                        recipe = recipe,
                                        onCardClick = { onRecipeClick(recipe.id) }
                                    )
                                }
                            }
                            if (rowRecipes.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun WoopperRecipeCard(recipe: Recipe, onCardClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth().aspectRatio(0.85f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E6)),
        border = BorderStroke(1.dp, Color(0xFFFFB3C1))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (!recipe.imagePath.isNullOrBlank()) {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize().clip(
                            androidx.compose.foundation.shape.RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp
                            )
                        ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recipe.name.take(1).uppercase(),
                            fontSize = 32.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().background(Peach).padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipe.name,
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

@Composable
fun TimerTopBar(
    minutes: Int,
    seconds: Int,
    isRunning: Boolean,
    onTogglePause: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxWidth(), color = Color(0xFFFFB5A7), shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer: ${String.format(Locale.getDefault(), "%02d", minutes)}:${String.format(Locale.getDefault(), "%02d", seconds)}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePause) {
                    if (isRunning) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(6.dp).height(16.dp).background(Color.White))
                            Box(Modifier.width(6.dp).height(16.dp).background(Color.White))
                        }
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White)
                    }
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                }
            }
        }
    }
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { if (minutes < 99) onMinutesChange(minutes + 1) }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White)
                }
                Text(String.format(Locale.getDefault(), "%02d", minutes), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = { if (minutes > 0) onMinutesChange(minutes - 1) }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White)
                }
            }

            Text(":", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {
                    if (seconds < 50) onSecondsChange(seconds + 10)
                    else { onSecondsChange(0); if (minutes < 99) onMinutesChange(minutes + 1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = Color.White)
                }
                Text(String.format(Locale.getDefault(), "%02d", seconds), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = {
                    if (seconds > 0) onSecondsChange(seconds - 10)
                    else { onSecondsChange(50); if (minutes > 0) onMinutesChange(minutes - 1) }
                }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = Color.White)
                }
            }

            Spacer(Modifier.width(16.dp))

            FloatingActionButton(
                onClick = { if (minutes > 0 || seconds > 0) onStart() },
                containerColor = Color(0xFF4DD0E1),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        if (minutes > 0 || seconds > 0) {
            TextButton(onClick = { onMinutesChange(0); onSecondsChange(0) }) {
                Text("Reset", color = Color.White)
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
        Text("Servings:", color = Color.Black, fontWeight = FontWeight.Medium)

        AssistChip(
            onClick = { open = true },
            label = { Text(desiredServings.toString(), color = Color.Black, fontWeight = FontWeight.Bold) },
            colors = AssistChipDefaults.assistChipColors(containerColor = Peach, labelColor = Color.Black),
            border = BorderStroke(1.dp, Pink)
        )

        if (desiredServings != baseServings) {
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onDesiredChange(baseServings) },
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Pink),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Reset") }
        }
    }

    if (open) {
        val dialogFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Pink,
            unfocusedBorderColor = Pink.copy(alpha = 0.85f),
            cursorColor = Pink,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black.copy(alpha = 0.9f),
            errorBorderColor = Pink,
            errorLabelColor = Color.Black,
            errorCursorColor = Pink,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827)
        )

        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Adjust servings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Base recipe: $baseServings")
                    OutlinedTextField(
                        value = input,
                        onValueChange = { v ->
                            val filtered = v.filter { it.isDigit() }.take(3)
                            input = if (filtered.isBlank()) ""
                            else (filtered.toIntOrNull()?.coerceIn(1, 999) ?: baseServings).toString()
                        },
                        label = { Text("Servings") },
                        singleLine = true,
                        colors = dialogFieldColors
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
                Button(
                    onClick = { open = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Blue)
                ) { Text("Cancel") }
            },
            containerColor = Peach
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
    var desiredServings by remember(recipe.id) { mutableIntStateOf(baseServings) }
    val scaleFactor = desiredServings.toDouble() / baseServings.toDouble()

    Box(modifier = modifier.fillMaxSize().background(Peach)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Blue)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                        }
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF111827))
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Text(recipe.name, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Pink, modifier = Modifier.offset((-2).dp, 0.dp))
                            Text(recipe.name, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Pink, modifier = Modifier.offset((2).dp, 0.dp))
                            Text(recipe.name, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Pink, modifier = Modifier.offset(0.dp, (-2).dp))
                            Text(recipe.name, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Pink, modifier = Modifier.offset(0.dp, (2).dp))
                            Text(recipe.name, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = LightPink)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            if (!recipe.imagePath.isNullOrBlank()) {
                item {
                    AsyncImage(
                        model = recipe.imagePath,
                        contentDescription = "Recipe image",
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(recipe.mealType, color = Color.Black) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Peach, labelColor = Color.Black),
                                border = BorderStroke(1.dp, Pink)
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("${recipe.totalTime} min", color = Color.Black) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Peach, labelColor = Color.Black),
                                border = BorderStroke(1.dp, Pink)
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Difficulty ${recipe.difficulty}", color = Color.Black) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Peach, labelColor = Color.Black),
                                border = BorderStroke(1.dp, Pink)
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        CategoriesChips(categoriesCsv = recipe.categories)
                        Spacer(Modifier.height(16.dp))

                        ServingsAdjuster(
                            baseServings = baseServings,
                            desiredServings = desiredServings,
                            onDesiredChange = { desiredServings = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        Text("Ingredients:", color = Color.Black, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

                                Text(
                                    text = "• ${amountUnit.ifBlank { "" }} ${ing.name}".trim(),
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text("Instructions:", color = Color.Black, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recipe.instructions.sortedBy { it.stepNumber }.forEach { step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${step.stepNumber}. ${step.text}",
                                        modifier = Modifier.weight(1f),
                                        color = Color.Black
                                    )

                                    Box(
                                        modifier = Modifier.widthIn(min = 86.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (step.timer > 0) {
                                            Button(onClick = {
                                                timerViewModel.setMinutes(step.timer)
                                                timerViewModel.setSeconds(0)
                                                showTimerSheet = true
                                            }) { Text("${step.timer} min") }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text("Notes:", color = Color.Black, fontWeight = FontWeight.Medium)
                        Text(recipe.notes, color = Color.Black)

                        Spacer(Modifier.height(20.dp))

                        WoopperPrimaryButton(
                            text = "Set Timer",
                            onClick = { showTimerSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showTimerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTimerSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFFFFB5A7)
            ) {
                TimerComponent(
                    minutes = timerState.minutes,
                    seconds = timerState.seconds,
                    onMinutesChange = { timerViewModel.setMinutes(it) },
                    onSecondsChange = { timerViewModel.setSeconds(it) },
                    onStart = { timerViewModel.startTimer(); showTimerSheet = false },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WoopperFormScaffold(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            Surface(color = Blue, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        Text(title, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Pink, modifier = Modifier.offset((-2).dp, 0.dp))
                        Text(title, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Pink, modifier = Modifier.offset((2).dp, 0.dp))
                        Text(title, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Pink, modifier = Modifier.offset(0.dp, (-2).dp))
                        Text(title, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Pink, modifier = Modifier.offset(0.dp, (2).dp))
                        Text(title, fontSize = 36.sp, fontWeight = FontWeight.Black, color = LightPink)
                    }

                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = if (saveEnabled) Color(0xFF111827) else Color(0xFF111827).copy(alpha = 0.35f)
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { bottomBar?.invoke() },
        containerColor = Peach
    ) { padding -> content(padding) }
}


@Composable
private fun WoopperSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Pink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun WoopperQuickMetaRow(
    totalTime: String,
    onTotalTime: (String) -> Unit,
    difficulty: String,
    onDifficulty: (String) -> Unit,
    servingSize: String,
    onServingSize: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DigitsOnlyField(
            value = totalTime,
            onValueChange = onTotalTime,
            label = { Text("Total (min)") },
            modifier = Modifier.weight(1f),
            maxDigits = 4,
            min = 0
        )
        DigitsOnlyField(
            value = difficulty,
            onValueChange = onDifficulty,
            label = { Text("Diff 1–5") },
            modifier = Modifier.weight(1f),
            maxDigits = 1,
            min = 1,
            max = 5
        )
        DigitsOnlyField(
            value = servingSize,
            onValueChange = onServingSize,
            label = { Text("Servings") },
            modifier = Modifier.weight(1f),
            maxDigits = 3,
            min = 1
        )
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
    var notes by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    val instructions = remember { mutableStateListOf<InstructionDraft>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    var editingIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var editingInstructionIndex by remember { mutableStateOf<Int?>(null) }

    var nameError by remember { mutableStateOf(false) }
    var mealTypeError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    editingIngredientIndex?.takeIf { it in ingredients.indices }?.let { idx ->
        EditIngredientDialog(
            initial = ingredients[idx],
            onDismiss = { editingIngredientIndex = null },
            onSave = { updated -> ingredients[idx] = updated; editingIngredientIndex = null }
        )
    }

    editingInstructionIndex?.takeIf { it in instructions.indices }?.let { idx ->
        EditInstructionDialog(
            initial = instructions[idx],
            onDismiss = { editingInstructionIndex = null },
            onSave = { updated -> instructions[idx] = updated; editingInstructionIndex = null }
        )
    }

    fun validate(): Boolean {
        val nErr = name.trim().isBlank()
        val mErr = mealType.isBlank()
        nameError = nErr
        mealTypeError = mErr

        validationMessage = when {
            nErr && mErr -> "Please fill in: Name and Meal type"
            nErr -> "Please fill in: Name"
            mErr -> "Please fill in: Meal type"
            else -> null
        }

        return validationMessage == null
    }

    suspend fun doSave() {
        val trimmedName = name.trim()
        val total = totalTime.toIntOrNull() ?: 0
        val diff = (difficulty.toIntOrNull() ?: 1).coerceIn(1, 5)
        val serveSize = (servingSize.toIntOrNull() ?: 1).coerceAtLeast(1)

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

        val savedPath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }

        addRecipeViewModel.addRecipe(
            name = trimmedName,
            mealType = mealType,
            categories = categories.joinToString(","),
            imagePath = savedPath,
            ingredients = ingredientList,
            instructions = instructionList,
            notes = notes,
            totalTime = total,
            difficulty = diff,
            servingSize = serveSize
        )
        onFinished()
    }

    WoopperFormScaffold(
        title = "Create",
        onBack = onFinished,
        onSave = {
            if (!validate()) return@WoopperFormScaffold
            scope.launch { doSave() }
        },
        saveEnabled = name.trim().isNotBlank() && mealType.isNotBlank(),
        snackbarHostState = snackbarHostState,
        bottomBar = {
            validationMessage?.let { msg ->
                Surface(color = LightPink.copy(alpha = 0.55f), tonalElevation = 2.dp) {
                    Text(
                        text = msg,
                        color = Pink,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WoopperInputCard(title = "") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError) nameError = name.trim().isBlank()
                            if (validationMessage != null) validationMessage = null
                        },
                        label = { Text("Recipe name*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = woopperTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = nameError
                    )

                    MealTypeDropdown(
                        mealType = mealType,
                        onMealTypeChange = {
                            mealType = it
                            if (mealTypeError) mealTypeError = mealType.isBlank()
                            if (validationMessage != null) validationMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CategoriesMultiDropdown(
                        selected = categories,
                        onSelectedChange = { categories = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DigitsOnlyField(
                            value = totalTime,
                            onValueChange = { totalTime = it },
                            label = { Text("Total (min)") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 4,
                            min = 0,
                            colors = woopperTextFieldColors()
                        )
                        DigitsOnlyField(
                            value = difficulty,
                            onValueChange = { difficulty = it },
                            label = { Text("Diff 1–5") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 1,
                            min = 1,
                            max = 5,
                            colors = woopperTextFieldColors()
                        )
                        DigitsOnlyField(
                            value = servingSize,
                            onValueChange = { servingSize = it },
                            label = { Text("Servings") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 3,
                            min = 1,
                            colors = woopperTextFieldColors()
                        )
                    }
                }
            }

            item {
                WoopperInputCard(title = "Ingredients") {
                    IngredientsEditor(
                        ingredients = ingredients,
                        onAdd = { ingredients.add(it) },
                        onRemoveAt = { idx -> ingredients.removeAt(idx) },
                        onEditAt = { idx -> editingIngredientIndex = idx },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                WoopperInputCard(title = "Steps") {
                    InstructionsEditor(
                        instructions = instructions,
                        onAdd = { instructions.add(it) },
                        onRemoveAt = { idx -> instructions.removeAt(idx) },
                        onEditAt = { idx -> editingInstructionIndex = idx },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                WoopperInputCard(title = "") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = woopperTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3
                    )
                }
            }

            item {
                WoopperInputCard(title = "") {
                    ImagePickerField(
                        existingImagePath = null,
                        pickedImageUri = pickedImageUri,
                        onPick = { pickedImageUri = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        if (!validate()) return@Button
                        scope.launch { doSave() }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    enabled = true
                ) { Text("Save Recipe", fontWeight = FontWeight.Black) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    editingIngredientIndex?.takeIf { it in ingredients.indices }?.let { idx ->
        EditIngredientDialog(
            initial = ingredients[idx],
            onDismiss = { editingIngredientIndex = null },
            onSave = { updated -> ingredients[idx] = updated; editingIngredientIndex = null }
        )
    }

    editingInstructionIndex?.takeIf { it in instructions.indices }?.let { idx ->
        EditInstructionDialog(
            initial = instructions[idx],
            onDismiss = { editingInstructionIndex = null },
            onSave = { updated -> instructions[idx] = updated; editingInstructionIndex = null }
        )
    }

    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var mealTypeError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        val nErr = ui.name.trim().isBlank()
        val mErr = ui.mealType.isBlank()
        nameError = nErr
        mealTypeError = mErr

        validationMessage = when {
            nErr && mErr -> "Please fill in: Name and Meal type"
            nErr -> "Please fill in: Name"
            mErr -> "Please fill in: Meal type"
            else -> null
        }

        return validationMessage == null
    }

    suspend fun doSave() {
        val newPath = pickedImageUri?.let { copyImageToInternalStorage(context, it) }
        if (newPath != null) viewModel.updateImagePath(newPath)

        viewModel.save(
            ingredientDrafts = ingredients.toList(),
            instructionDrafts = instructions.toList(),
            onFinished = onSaved
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Peach,
            title = { Text("Delete recipe?", fontWeight = FontWeight.Black, color = Color(0xFF111827)) },
            text = { Text("This will permanently delete the recipe. This cannot be undone.", color = Color(0xFF111827)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete(onDeleted)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Color.White)
                ) { Text("Delete", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    border = BorderStroke(1.dp, Pink),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink)
                ) { Text("Cancel", fontWeight = FontWeight.Black) }
            }
        )
    }

    WoopperFormScaffold(
        title = "Edit",
        onBack = onSaved,
        onSave = {
            if (!validate()) return@WoopperFormScaffold
            scope.launch { doSave() }
        },
        saveEnabled = ui.name.trim().isNotBlank() && ui.mealType.isNotBlank(),
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Surface(color = LightPink.copy(alpha = 0.55f), tonalElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    validationMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = Pink,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f).height(54.dp),
                            border = BorderStroke(1.dp, Pink),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Pink),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Delete", fontWeight = FontWeight.Black) }

                        Button(
                            onClick = {
                                if (!validate()) return@Button
                                scope.launch { doSave() }
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            enabled = true
                        ) { Text("Save", fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WoopperInputCard(title = "") {
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = {
                            viewModel.updateName(it)
                            if (nameError) nameError = ui.name.trim().isBlank()
                            if (validationMessage != null) validationMessage = null
                        },
                        label = { Text("Recipe name*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = woopperTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        isError = nameError
                    )

                    MealTypeDropdown(
                        mealType = ui.mealType,
                        onMealTypeChange = {
                            viewModel.updateMealType(it)
                            if (mealTypeError) mealTypeError = ui.mealType.isBlank()
                            if (validationMessage != null) validationMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    CategoriesMultiDropdown(
                        selected = ui.categories,
                        onSelectedChange = viewModel::updateCategories,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DigitsOnlyField(
                            value = ui.totalTime,
                            onValueChange = viewModel::updateTotalTime,
                            label = { Text("Total (min)") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 4,
                            min = 0,
                            colors = woopperTextFieldColors()
                        )
                        DigitsOnlyField(
                            value = ui.difficulty,
                            onValueChange = viewModel::updateDifficulty,
                            label = { Text("Diff 1–5") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 1,
                            min = 1,
                            max = 5,
                            colors = woopperTextFieldColors()
                        )
                        DigitsOnlyField(
                            value = ui.servingSize,
                            onValueChange = viewModel::updateServingSize,
                            label = { Text("Servings") },
                            modifier = Modifier.weight(1f),
                            maxDigits = 3,
                            min = 1,
                            colors = woopperTextFieldColors()
                        )
                    }
                }
            }

            item {
                WoopperInputCard(title = "Ingredients") {
                    IngredientsEditor(
                        ingredients = ingredients,
                        onAdd = { ingredients.add(it) },
                        onRemoveAt = { idx -> ingredients.removeAt(idx) },
                        onEditAt = { idx -> editingIngredientIndex = idx },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                WoopperInputCard(title = "Steps") {
                    InstructionsEditor(
                        instructions = instructions,
                        onAdd = { instructions.add(it) },
                        onRemoveAt = { idx -> instructions.removeAt(idx) },
                        onEditAt = { idx -> editingInstructionIndex = idx },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                WoopperInputCard(title = "") {
                    OutlinedTextField(
                        value = ui.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = woopperTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3
                    )
                }
            }

            item {
                WoopperInputCard(title = "") {
                    ImagePickerField(
                        existingImagePath = ui.imagePath,
                        pickedImageUri = pickedImageUri,
                        onPick = { pickedImageUri = it },
                        onRemoveExisting = { viewModel.updateImagePath(null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}