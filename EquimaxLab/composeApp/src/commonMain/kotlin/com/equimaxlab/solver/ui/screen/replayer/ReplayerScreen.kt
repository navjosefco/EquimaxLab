package com.equimaxlab.solver.ui.screen.replayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.presentation.replayer.*
import com.equimaxlab.solver.ui.components.*
import com.equimaxlab.solver.ui.theme.*

private enum class PickerTarget { HERO, VILLAIN }
private data class VillainPickerTarget(val playerName: String)

@Composable
fun ReplayerScreen(
    viewModel: ReplayerViewModel = viewModel { ReplayerViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.step) {
        ReplayerStep.SETUP   -> SetupStep(state, viewModel)
        ReplayerStep.STREETS -> StreetsStep(state, viewModel)
        ReplayerStep.SUMMARY -> SummaryStep(state, viewModel)
    }
}

// ---- SETUP ----

@Composable
private fun SetupStep(
    state: ReplayerUiState,
    viewModel: ReplayerViewModel
) {
    var heroPickerOpen    by remember { mutableStateOf(false) }
    var villainPicker     by remember { mutableStateOf<String?>(null) }
    var newPlayerName     by remember { mutableStateOf("") }
    var showAddPlayer     by remember { mutableStateOf(false) }
    var importMode        by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(GreenFelt)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Replayer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary, fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(false to "Manual", true to "Importar").forEach { (isImport, label) ->
                        val active = importMode == isImport
                        Surface(
                            onClick = { importMode = isImport },
                            shape   = RoundedCornerShape(20.dp),
                            color   = if (active) GreenAccent else GreenFeltLight
                        ) {
                            Text(
                                text     = label,
                                color    = if (active) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (importMode) {
                // Modo importar
                Text("PEGA EL HISTORIAL", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                OutlinedTextField(
                    value         = state.importText,
                    onValueChange = { viewModel.setImportText(it) },
                    placeholder   = { Text("Pega el historial de PokerStars o GGPoker...", color = TextSecondary, fontSize = 12.sp) },
                    minLines      = 5,
                    maxLines      = 10,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = GreenAccent,
                        unfocusedBorderColor    = DividerColor,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary,
                        cursorColor             = GreenAccent,
                        focusedContainerColor   = GreenFeltLight,
                        unfocusedContainerColor = GreenFeltLight
                    ),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                state.importError?.let { Text(it, color = RedAccent, fontSize = 12.sp) }
                Button(
                    onClick  = { viewModel.parseImport() },
                    enabled  = state.importText.isNotBlank(),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("ANALIZAR MANO", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                // Jugadores
                SectionLabel("JUGADORES (máx 6)")
                Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        state.players.forEach { player ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (player.isHero) "⭐ ${player.name}" else player.name,
                                    color = if (player.isHero) GoldAccent else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (player.isHero) FontWeight.Bold else FontWeight.Normal
                                )
                                if (!player.isHero && state.players.size > 2) {
                                    TextButton(onClick = { viewModel.removePlayer(player.name) }) {
                                        Text("Quitar", color = RedAccent, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        if (state.players.size < 6) {
                            if (showAddPlayer) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value         = newPlayerName,
                                        onValueChange = { newPlayerName = it },
                                        placeholder   = { Text("Nombre del jugador", color = TextSecondary, fontSize = 12.sp) },
                                        singleLine    = true,
                                        colors        = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor      = GreenAccent,
                                            unfocusedBorderColor    = DividerColor,
                                            focusedTextColor        = TextPrimary,
                                            unfocusedTextColor      = TextPrimary,
                                            cursorColor             = GreenAccent,
                                            focusedContainerColor   = GreenFeltLight,
                                            unfocusedContainerColor = GreenFeltLight
                                        ),
                                        shape    = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            if (newPlayerName.isNotBlank()) {
                                                viewModel.addPlayer(newPlayerName.trim())
                                                newPlayerName = ""
                                                showAddPlayer = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                                        shape  = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                TextButton(onClick = { showAddPlayer = true }) {
                                    Text("+ Añadir jugador", color = GreenAccent, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Cartas del héroe
                SectionLabel("TUS CARTAS")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) { i ->
                        val card = state.heroCards.getOrNull(i)
                        if (card != null)
                            CardView(card = card, size = CardSize.LARGE,
                                onClick = { viewModel.removeHeroCard(card) })
                        else
                            EmptyCardSlot(size = CardSize.LARGE,
                                onClick = { heroPickerOpen = true })
                    }
                }

                // Cartas de rivales
                state.players.filter { !it.isHero }.forEach { player ->
                    SectionLabel("CARTAS DE ${player.name.uppercase()}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val cards = state.villainCards[player.name] ?: emptyList()
                        repeat(2) { i ->
                            val card = cards.getOrNull(i)
                            if (card != null)
                                CardView(card = card, size = CardSize.MEDIUM,
                                    onClick = { viewModel.removeVillainCard(player.name, card) })
                            else
                                EmptyCardSlot(size = CardSize.MEDIUM,
                                    onClick = { villainPicker = player.name })
                        }
                    }
                }

                // Botón continuar
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = { viewModel.proceedToStreets() },
                    enabled  = state.heroCards.size == 2,
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("CONTINUAR →", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            // Manos guardadas
            if (state.savedHands.isNotEmpty()) {
                SectionLabel("MANOS GUARDADAS")
                state.savedHands.forEach { hand ->
                    SavedHandCard(
                        hand     = hand,
                        onDelete = { viewModel.deleteSavedHand(hand.id) }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // Pickers
    val blocked = state.heroCards + state.villainCards.values.flatten()
    if (heroPickerOpen) {
        CardPickerDialog(
            blockedCards   = blocked,
            onCardSelected = { viewModel.addHeroCard(it) },
            onDismiss      = { heroPickerOpen = false }
        )
    }
    villainPicker?.let { name ->
        CardPickerDialog(
            blockedCards   = blocked,
            onCardSelected = { viewModel.addVillainCard(name, it) },
            onDismiss      = { villainPicker = null }
        )
    }
}

// ---- STREETS ----

@Composable
private fun StreetsStep(
    state: ReplayerUiState,
    viewModel: ReplayerViewModel
) {
    var boardPickerOpen  by remember { mutableStateOf(false) }
    var actionDialog     by remember { mutableStateOf(false) }
    var currentPlayer    by remember { mutableStateOf(0) }

    val neededCards = if (state.streets.isEmpty()) 3 else 1
    val pendingFull = state.pendingStreetCards.size >= neededCards
    val allActed    = pendingFull && state.pendingActions.size >= state.players.size

    Box(modifier = Modifier.fillMaxSize().background(GreenFelt)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Replayer", style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary, fontWeight = FontWeight.Bold)
                if (state.streets.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(20.dp), color = GreenFeltLight) {
                        Text(
                            text = streetName(state.streets.size - 1),
                            color = GoldAccent, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Cartas del héroe
            SectionLabel("TUS CARTAS")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.heroCards.forEach { CardView(card = it, size = CardSize.MEDIUM) }
                state.players.filter { !it.isHero }.forEach { player ->
                    val cards = state.villainCards[player.name] ?: emptyList()
                    if (cards.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text("VS", color = TextSecondary, fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))
                        cards.forEach { CardView(card = it, size = CardSize.MEDIUM) }
                    }
                }
            }

            // Tabs de calles
            if (state.streets.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.streets.forEachIndexed { i, street ->
                        val active = i == state.currentStreet
                        Surface(
                            onClick = { viewModel.goToStreet(i) },
                            shape   = RoundedCornerShape(6.dp),
                            color   = if (active) GreenAccent else GreenFeltLight
                        ) {
                            Text(
                                text = street.name,
                                color = if (active) Color.Black else TextSecondary,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Contenido de la calle seleccionada
                val currentStreetData = state.streets.getOrNull(state.currentStreet)
                currentStreetData?.let { street ->
                    Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Board
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                street.cards.forEach { CardView(card = it, size = CardSize.SMALL) }
                            }
                            // Acciones
                            if (street.actions.isNotEmpty()) {
                                HorizontalDivider(color = DividerColor)
                                street.actions.forEach { action ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = action.playerName,
                                            color = if (action.playerName == "Tú") GoldAccent else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (action.playerName == "Tú") FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = action.displayText(),
                                            color = actionColor(action.action),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.streets.isNotEmpty()) {
                    TextButton(
                        onClick  = { viewModel.removeLastStreet() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("← Quitar ${state.streets.last().name}",
                            color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // Añadir siguiente calle
            if (state.streets.size < 3) {
                val pending = state.pendingStreetCards.size
                val label   = when (state.streets.size) {
                    0    -> "+ Flop ($pending/3)"
                    1    -> "+ Turn ($pending/1)"
                    else -> "+ River ($pending/1)"
                }

                if (!pendingFull) {
                    Surface(
                        onClick = { boardPickerOpen = true },
                        shape   = RoundedCornerShape(6.dp),
                        color   = GreenFeltLight,
                        border  = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                    ) {
                        Text(label, color = TextSecondary, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    // Cartas pending
                    if (state.pendingStreetCards.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            state.pendingStreetCards.forEach { card ->
                                CardView(card = card, size = CardSize.MEDIUM,
                                    onClick = { viewModel.removePendingCard(card) })
                            }
                        }
                    }
                } else {
                    // Recoger acciones de cada jugador
                    val nextPlayerIndex = state.pendingActions.size
                    if (nextPlayerIndex < state.players.size) {
                        val nextPlayer = state.players[nextPlayerIndex]
                        Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Acción de ${nextPlayer.name}",
                                    color = if (nextPlayer.isHero) GoldAccent else TextPrimary,
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                                ActionButtons(
                                    playerName = nextPlayer.name,
                                    onAction   = { viewModel.addAction(it) }
                                )
                            }
                        }
                    } else {
                        // Todas las acciones recogidas — confirmar calle
                        Button(
                            onClick  = { viewModel.commitStreet() },
                            colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                            shape    = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("CONFIRMAR ${streetNameNext(state.streets.size).uppercase()}",
                                color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Botones finales
            if (state.streets.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = { viewModel.proceedToSummary() },
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("VER RESUMEN Y GUARDAR",
                        color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            TextButton(
                onClick  = { viewModel.reset() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Nueva mano", color = TextSecondary)
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // Board picker
    val blockedAll = state.heroCards + state.villainCards.values.flatten() +
            state.streets.flatMap { it.cards } + state.pendingStreetCards
    if (boardPickerOpen) {
        CardPickerDialog(
            blockedCards   = blockedAll,
            onCardSelected = {
                viewModel.addPendingCard(it)
                if (state.streets.isEmpty() && state.pendingStreetCards.size + 1 >= 3) boardPickerOpen = false
                else if (state.streets.isNotEmpty()) boardPickerOpen = false
            },
            onDismiss = { boardPickerOpen = false }
        )
    }
}

@Composable
private fun ActionButtons(
    playerName: String,
    onAction: (PlayerActionEntry) -> Unit
) {
    var showBetInput  by remember { mutableStateOf(false) }
    var betAmount     by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<PlayerAction?>(null) }

    if (showBetInput) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = betAmount,
                onValueChange = { betAmount = it },
                placeholder   = { Text("Cantidad en bb", color = TextSecondary, fontSize = 12.sp) },
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = GreenAccent,
                    unfocusedBorderColor    = DividerColor,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = GreenAccent,
                    focusedContainerColor   = GreenFeltLight,
                    unfocusedContainerColor = GreenFeltLight
                ),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val amount = betAmount.toDoubleOrNull()
                    onAction(PlayerActionEntry(playerName, pendingAction!!, amount))
                    showBetInput  = false
                    betAmount     = ""
                    pendingAction = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape  = RoundedCornerShape(8.dp)
            ) {
                Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                PlayerAction.FOLD   to "Fold",
                PlayerAction.CHECK  to "Check",
                PlayerAction.CALL   to "Call"
            ).forEach { (action, label) ->
                Surface(
                    onClick = { onAction(PlayerActionEntry(playerName, action)) },
                    shape   = RoundedCornerShape(8.dp),
                    color   = GreenFelt,
                    border  = androidx.compose.foundation.BorderStroke(1.dp, actionColor(action)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, color = actionColor(action), fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                PlayerAction.BET    to "Bet",
                PlayerAction.RAISE  to "Raise",
                PlayerAction.ALL_IN to "All-in"
            ).forEach { (action, label) ->
                Surface(
                    onClick = {
                        if (action == PlayerAction.ALL_IN) {
                            onAction(PlayerActionEntry(playerName, action))
                        } else {
                            pendingAction = action
                            showBetInput  = true
                        }
                    },
                    shape   = RoundedCornerShape(8.dp),
                    color   = GreenFelt,
                    border  = androidx.compose.foundation.BorderStroke(1.dp, actionColor(action)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, color = actionColor(action), fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp))
                }
            }
        }
    }
}

// ---- SUMMARY ----

@Composable
private fun SummaryStep(
    state: ReplayerUiState,
    viewModel: ReplayerViewModel
) {
    var notes         by remember { mutableStateOf("") }
    var saved         by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(GreenFelt)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resumen", style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.goBackToStreets() }) {
                    Text("← Volver", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Cartas
            SectionLabel("MANO")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.heroCards.forEach { CardView(card = it, size = CardSize.MEDIUM) }
                state.players.filter { !it.isHero }.forEach { player ->
                    val cards = state.villainCards[player.name] ?: emptyList()
                    if (cards.isNotEmpty()) {
                        Text("VS", color = TextSecondary, fontSize = 11.sp)
                        cards.forEach { CardView(card = it, size = CardSize.MEDIUM) }
                    }
                }
            }

            // Calles
            state.streets.forEach { street ->
                Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(street.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            street.cards.forEach { CardView(card = it, size = CardSize.SMALL) }
                        }
                        if (street.actions.isNotEmpty()) {
                            HorizontalDivider(color = DividerColor)
                            street.actions.forEach { action ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = action.playerName,
                                        color = if (action.playerName == "Tú") GoldAccent else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (action.playerName == "Tú") FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = action.displayText(),
                                        color = actionColor(action.action),
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notas
            SectionLabel("NOTAS (OPCIONAL)")
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                placeholder   = { Text("Añade notas sobre la mano...", color = TextSecondary, fontSize = 12.sp) },
                minLines      = 3,
                maxLines      = 5,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = GreenAccent,
                    unfocusedBorderColor    = DividerColor,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = GreenAccent,
                    focusedContainerColor   = GreenFeltLight,
                    unfocusedContainerColor = GreenFeltLight
                ),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Guardar
            if (!saved) {
                Button(
                    onClick = {
                        viewModel.saveHand(notes)
                        saved = true
                    },
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("GUARDAR MANO", color = Color.Black,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GreenAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        "✓ Mano guardada",
                        color = GreenAccent, fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }

            Button(
                onClick  = { viewModel.reset() },
                colors   = ButtonDefaults.buttonColors(containerColor = GreenFeltLight),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("NUEVA MANO", color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ---- Componentes auxiliares ----

@Composable
private fun SavedHandCard(
    hand: SavedHandEntry,
    onDelete: () -> Unit
) {
    Surface(shape = RoundedCornerShape(10.dp), color = GreenFeltLight) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text     = hand.label,
                        color    = TextSecondary,
                        fontSize = 10.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        hand.heroCards.forEach { CardView(card = it, size = CardSize.SMALL) }
                        if (hand.villainCards.isNotEmpty()) {
                            Text(
                                "VS", color = TextSecondary, fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            hand.villainCards.take(2).forEach {
                                CardView(card = it, size = CardSize.SMALL)
                            }
                        }
                    }
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar", color = RedAccent, fontSize = 11.sp)
                }
            }
            hand.streets.forEach { street ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        street.name, color = TextSecondary, fontSize = 10.sp,
                        modifier = Modifier.width(36.dp)
                    )
                    street.cards.forEach { CardView(card = it, size = CardSize.SMALL) }
                }
            }
            if (hand.notes.isNotBlank()) {
                Text(hand.notes, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
}

private fun PlayerActionEntry.displayText(): String {
    val actionStr = when (action) {
        PlayerAction.FOLD   -> "Fold"
        PlayerAction.CHECK  -> "Check"
        PlayerAction.CALL   -> "Call"
        PlayerAction.BET    -> "Bet"
        PlayerAction.RAISE  -> "Raise"
        PlayerAction.ALL_IN -> "All-in"
    }
    return if (amountBb != null) "$actionStr ${amountBb}bb" else actionStr
}

private fun actionColor(action: PlayerAction): Color = when (action) {
    PlayerAction.FOLD   -> RedAccent
    PlayerAction.CHECK  -> TextSecondary
    PlayerAction.CALL   -> Color(0xFF27AE60)
    PlayerAction.BET    -> GoldAccent
    PlayerAction.RAISE  -> GreenAccent
    PlayerAction.ALL_IN -> Color(0xFFE67E22)
}

private fun streetName(index: Int) = when (index) {
    0 -> "Flop"; 1 -> "Turn"; else -> "River"
}

private fun streetNameNext(count: Int) = when (count) {
    0 -> "Flop"; 1 -> "Turn"; else -> "River"
}