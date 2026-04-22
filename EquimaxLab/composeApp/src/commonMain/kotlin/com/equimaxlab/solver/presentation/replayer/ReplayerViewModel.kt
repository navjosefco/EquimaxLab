package com.equimaxlab.solver.presentation.replayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equimaxlab.solver.core.Card
import com.equimaxlab.solver.core.Rank
import com.equimaxlab.solver.core.Suit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class ReplayerInputMode { MANUAL, IMPORT }
enum class ReplayerStep { SETUP, STREETS, SUMMARY }

data class StreetData(
    val name: String,
    val cards: List<Card>,
    val actions: List<PlayerActionEntry> = emptyList()
)

data class ReplayerUiState(
    val mode: ReplayerInputMode = ReplayerInputMode.MANUAL,
    val step: ReplayerStep = ReplayerStep.SETUP,

    // Jugadores
    val players: List<Player> = listOf(
        Player("Tú", isHero = true),
        Player("Rival 1")
    ),

    // Cartas
    val heroCards: List<Card> = emptyList(),
    val villainCards: Map<String, List<Card>> = emptyMap(),

    // Calles
    val streets: List<StreetData> = emptyList(),
    val currentStreet: Int = 0,

    // Acción en construcción
    val pendingStreetCards: List<Card> = emptyList(),
    val pendingActions: List<PlayerActionEntry> = emptyList(),
    val currentActionPlayer: Int = 0,

    // Import
    val importText: String = "",
    val importError: String? = null,

    // Guardadas
    val savedHands: List<SavedHandEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReplayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReplayerUiState())
    val uiState: StateFlow<ReplayerUiState> = _uiState.asStateFlow()

    // ---- Jugadores ----

    fun addPlayer(name: String) {
        val state = _uiState.value
        if (state.players.size >= 6) return
        _uiState.update {
            it.copy(players = it.players + Player(name))
        }
    }

    fun removePlayer(name: String) {
        val state = _uiState.value
        if (state.players.size <= 2) return
        _uiState.update {
            it.copy(players = it.players.filter { p -> p.name != name })
        }
    }

    // ---- Cartas ----

    fun addHeroCard(card: Card) {
        val current = _uiState.value.heroCards
        if (current.size >= 2 || card in current) return
        _uiState.update { it.copy(heroCards = current + card) }
    }

    fun removeHeroCard(card: Card) {
        _uiState.update { it.copy(heroCards = it.heroCards - card) }
    }

    fun addVillainCard(playerName: String, card: Card) {
        val state   = _uiState.value
        val current = state.villainCards[playerName] ?: emptyList()
        if (current.size >= 2 || card in current) return
        _uiState.update {
            it.copy(villainCards = it.villainCards + (playerName to current + card))
        }
    }

    fun removeVillainCard(playerName: String, card: Card) {
        val state   = _uiState.value
        val current = state.villainCards[playerName] ?: return
        _uiState.update {
            it.copy(villainCards = it.villainCards + (playerName to current - card))
        }
    }

    // ---- Flujo de calles ----

    fun proceedToStreets() {
        if (_uiState.value.heroCards.size < 2) return
        _uiState.update { it.copy(step = ReplayerStep.STREETS) }
    }

    fun addPendingCard(card: Card) {
        val state  = _uiState.value
        val needed = if (state.streets.isEmpty()) 3 else 1
        val updated = state.pendingStreetCards + card
        _uiState.update { it.copy(pendingStreetCards = updated) }
        if (updated.size >= needed) {
            // Iniciar recogida de acciones
            _uiState.update {
                it.copy(
                    pendingActions      = emptyList(),
                    currentActionPlayer = 0
                )
            }
        }
    }

    fun removePendingCard(card: Card) {
        _uiState.update { it.copy(pendingStreetCards = it.pendingStreetCards - card) }
    }

    fun addAction(entry: PlayerActionEntry) {
        val state = _uiState.value
        val updated = state.pendingActions + entry
        _uiState.update {
            it.copy(
                pendingActions      = updated,
                currentActionPlayer = it.currentActionPlayer + 1
            )
        }
    }

    fun skipAction(playerName: String) {
        addAction(PlayerActionEntry(playerName, PlayerAction.CHECK))
    }

    fun commitStreet() {
        val state = _uiState.value
        val streetName = when (state.streets.size) {
            0    -> "Flop"
            1    -> "Turn"
            else -> "River"
        }
        val street = StreetData(
            name    = streetName,
            cards   = state.pendingStreetCards,
            actions = state.pendingActions
        )
        _uiState.update {
            it.copy(
                streets             = it.streets + street,
                currentStreet       = it.streets.size,
                pendingStreetCards  = emptyList(),
                pendingActions      = emptyList(),
                currentActionPlayer = 0
            )
        }
    }

    fun goToStreet(index: Int) {
        _uiState.update {
            it.copy(currentStreet = index.coerceIn(0, it.streets.size - 1))
        }
    }

    fun removeLastStreet() {
        _uiState.update {
            it.copy(
                streets       = it.streets.dropLast(1),
                currentStreet = maxOf(0, it.streets.size - 2)
            )
        }
    }

    fun proceedToSummary() {
        _uiState.update { it.copy(step = ReplayerStep.SUMMARY) }
    }

    fun goBackToStreets() {
        _uiState.update { it.copy(step = ReplayerStep.STREETS) }
    }

    // ---- Guardar mano ----

    @OptIn(ExperimentalUuidApi::class)
    fun saveHand(notes: String = "") {
        val state = _uiState.value
        if (state.heroCards.size < 2) return

        val hand = createSavedHand(
            id           = Uuid.random().toString(),
            heroCards    = state.heroCards,
            villainCards = state.villainCards.values.flatten(),
            players      = state.players.map { it.name },
            streets      = state.streets.map { street ->
                StreetEntry(
                    name    = street.name,
                    cards   = street.cards,
                    actions = street.actions
                )
            },
            notes = notes
        )

        val updated = (listOf(hand) + state.savedHands).take(5)
        _uiState.update { it.copy(savedHands = updated) }
    }

    private fun createSavedHand(
        id: String,
        heroCards: List<Card>,
        villainCards: List<Card>,
        players: List<String>,
        streets: List<StreetEntry>,
        notes: String
    ): SavedHandEntry {
        val handNumber = _uiState.value.savedHands.size + 1
        return SavedHandEntry(
            id           = id,
            label        = "Mano #$handNumber",
            heroCards    = heroCards,
            villainCards = villainCards,
            players      = players,
            streets      = streets,
            notes        = notes
        )
    }

    fun deleteSavedHand(id: String) {
        _uiState.update {
            it.copy(savedHands = it.savedHands.filter { h -> h.id != id })
        }
    }

    // ---- Import ----

    fun setMode(mode: ReplayerInputMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun setImportText(text: String) {
        _uiState.update { it.copy(importText = text, importError = null) }
    }

    fun parseImport() {
        val text  = _uiState.value.importText
        val lines = text.lines()
        try {
            val heroLine   = lines.firstOrNull { it.contains("Dealt to") }
            val boardLine  = lines.firstOrNull { it.trimStart().startsWith("Board") }
            val heroCards  = heroLine?.let { parseCardsInBrackets(it) } ?: emptyList()
            val boardCards = boardLine?.let { parseCardsInBrackets(it) } ?: emptyList()

            if (heroCards.size != 2) {
                _uiState.update {
                    it.copy(importError = "No se encontraron tus cartas. Pega el historial completo.")
                }
                return
            }

            _uiState.update {
                it.copy(
                    heroCards   = heroCards,
                    streets     = emptyList(),
                    importError = null,
                    step        = ReplayerStep.STREETS
                )
            }

            if (boardCards.size >= 3) {
                _uiState.update { it.copy(pendingStreetCards = boardCards.take(3)) }
                commitStreet()
            }

        } catch (e: Exception) {
            _uiState.update { it.copy(importError = "Error al parsear: ${e.message}") }
        }
    }

    private fun parseCardsInBrackets(line: String): List<Card> {
        val match = Regex("""\[([^\]]+)]""").find(line) ?: return emptyList()
        return match.groupValues[1].trim().split(" ").mapNotNull { parseCard(it.trim()) }
    }

    private fun parseCard(token: String): Card? {
        if (token.length < 2) return null
        val rankChar = token.dropLast(1).uppercase()
        val suitChar = token.last().lowercase()
        val rank = when (rankChar) {
            "2" -> Rank.TWO;   "3" -> Rank.THREE; "4" -> Rank.FOUR
            "5" -> Rank.FIVE;  "6" -> Rank.SIX;   "7" -> Rank.SEVEN
            "8" -> Rank.EIGHT; "9" -> Rank.NINE;  "T" -> Rank.TEN
            "J" -> Rank.JACK;  "Q" -> Rank.QUEEN; "K" -> Rank.KING
            "A" -> Rank.ACE;   else -> return null
        }
        val suit = when (suitChar) {
            "c" -> Suit.CLUBS; "d" -> Suit.DIAMONDS
            "h" -> Suit.HEARTS; "s" -> Suit.SPADES
            else -> return null
        }
        return Card(rank, suit)
    }

    // ---- Reset ----

    fun reset() {
        _uiState.value = ReplayerUiState(savedHands = _uiState.value.savedHands)
    }
}