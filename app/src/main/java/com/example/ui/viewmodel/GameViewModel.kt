package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class GameScreen {
    Home,
    LevelSelect,
    Gameplay,
    Leaderboard,
    DailyChallenge
}

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Navigation and screen routing
    private val _currentScreen = MutableStateFlow(GameScreen.Home)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Backstack history to support hygiene navigation (Back-button)
    private val navigationHistory = Stack<GameScreen>()

    // User statistics
    val userStats: StateFlow<UserStats> = repository.userStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats())

    // Leaderboard
    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboardEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Level State (Gameplay)
    private val _activeLevel = MutableStateFlow<LevelSpec?>(null)
    val activeLevel: StateFlow<LevelSpec?> = _activeLevel.asStateFlow()

    // Solved grid words for active level
    private val _solvedWords = MutableStateFlow<Set<String>>(emptySet())
    val solvedWords: StateFlow<Set<String>> = _solvedWords.asStateFlow()

    // Grid coordinates forced-revealed individually via hints/boosters
    private val _revealedCoords = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    val revealedCoords: StateFlow<Set<Pair<Int, Int>>> = _revealedCoords.asStateFlow()

    // Current letters wheel layout (which can be shuffled!)
    private val _wheelLetters = MutableStateFlow<List<Char>>(emptyList())
    val wheelLetters: StateFlow<List<Char>> = _wheelLetters.asStateFlow()

    // Swipe interaction feedback strings
    private val _currentSwipeText = MutableStateFlow("")
    val currentSwipeText: StateFlow<String> = _currentSwipeText.asStateFlow()

    private val _swipeErrorTrigger = MutableStateFlow(false)
    val swipeErrorTrigger: StateFlow<Boolean> = _swipeErrorTrigger.asStateFlow()

    // Dynamic banner celebratory and toast feedback states
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _celebrationMilestone = MutableStateFlow<String?>(null)
    val celebrationMilestone: StateFlow<String?> = _celebrationMilestone.asStateFlow()

    // Map of found Extra Words during this level
    private val _foundExtraWords = MutableStateFlow<Set<String>>(emptySet())
    val foundExtraWords: StateFlow<Set<String>> = _foundExtraWords.asStateFlow()

    // Active Daily Challenge State
    private val _dailyLevel = MutableStateFlow<LevelSpec?>(null)
    val dailyLevel: StateFlow<LevelSpec?> = _dailyLevel.asStateFlow()

    private val _dailySolvedWords = MutableStateFlow<Set<String>>(emptySet())
    val dailySolvedWords: StateFlow<Set<String>> = _dailySolvedWords.asStateFlow()

    private val _dailyCompleted = MutableStateFlow(false)
    val dailyCompleted: StateFlow<Boolean> = _dailyCompleted.asStateFlow()

    private val _dailyWheelLetters = MutableStateFlow<List<Char>>(emptyList())
    val dailyWheelLetters: StateFlow<List<Char>> = _dailyWheelLetters.asStateFlow()

    private val _dailyRevealedCoords = MutableStateFlow<Set<Pair<Int, Int>>>(emptySet())
    val dailyRevealedCoords: StateFlow<Set<Pair<Int, Int>>> = _dailyRevealedCoords.asStateFlow()

    init {
        // Prepare initial setups
        navigate(GameScreen.Home)
    }

    fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Navigation core
    fun navigate(screen: GameScreen) {
        val current = _currentScreen.value
        if (current != screen) {
            navigationHistory.push(current)
            _currentScreen.value = screen
        }

        // Initialize state based on screen entry
        when (screen) {
            GameScreen.Gameplay -> {
                // Ensure a level is active
                if (_activeLevel.value == null) {
                    val currentLevelId = userStats.value.currentLevelId
                    loadLevel(currentLevelId)
                }
            }
            GameScreen.DailyChallenge -> {
                loadDailyChallenge()
            }
            else -> {}
        }
    }

    fun handleBackPress(): Boolean {
        if (!navigationHistory.isEmpty()) {
            _currentScreen.value = navigationHistory.pop()
            return true
        }
        return false
    }

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            repository.updatePlayerName(name)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSoundEnabled(enabled)
        }
    }

    fun updateHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticEnabled(enabled)
        }
    }

    // Load spec of standard level
    fun loadLevel(levelId: Int) {
        val level = StaticLevelSource.levels.firstOrNull { it.id == levelId } ?: StaticLevelSource.levels.first()
        _activeLevel.value = level
        _wheelLetters.value = level.letters.toList()
        _currentSwipeText.value = ""
        _toastMessage.value = null
        _celebrationMilestone.value = null
        _foundExtraWords.value = emptySet()

        // Sync with existing gameplay state in database
        viewModelScope.launch {
            val savedState = repository.getLevelGameplayStateDirect(level.id)
            if (savedState != null) {
                val solved = savedState.solvedWordsSeparated.split(",")
                    .filter { it.isNotBlank() }
                    .toSet()
                _solvedWords.value = solved

                val coords = savedState.revealedCoordsSeparated.split(";")
                    .filter { it.isNotBlank() }
                    .map {
                        val parts = it.split(",")
                        Pair(parts[0].toInt(), parts[1].toInt())
                    }
                    .toSet()
                _revealedCoords.value = coords
            } else {
                _solvedWords.value = emptySet()
                _revealedCoords.value = emptySet()
            }
        }
    }

    // Load daily challenge
    private fun loadDailyChallenge() {
        val todayStr = getTodayDateStr()
        val level = DailyChallengeSource.getDailyLevel(todayStr)
        _dailyLevel.value = level
        _dailyWheelLetters.value = level.letters.toList()
        _dailyRevealedCoords.value = emptySet()
        _currentSwipeText.value = ""
        _toastMessage.value = null
        _celebrationMilestone.value = null

        viewModelScope.launch {
            val savedState = repository.getDailyChallengeStateDirect(todayStr)
            if (savedState != null) {
                _dailySolvedWords.value = savedState.solvedWordsSeparated.split(",")
                    .filter { it.isNotBlank() }
                    .toSet()
                _dailyCompleted.value = savedState.isCompleted
            } else {
                _dailySolvedWords.value = emptySet()
                _dailyCompleted.value = false
            }
        }
    }

    // Shuffles the circular letter nodes
    fun shuffleWheel() {
        if (_currentScreen.value == GameScreen.DailyChallenge) {
            _dailyWheelLetters.value = _dailyWheelLetters.value.shuffled()
        } else {
            _wheelLetters.value = _wheelLetters.value.shuffled()
        }
        showToast("Letters Shuffled!")
    }

    // Tracks swiping updates in real time
    fun updateSwipeText(text: String) {
        _currentSwipeText.value = text
    }

    // Handles terminal gesture validation
    fun validateSwipe(word: String) {
        val wordUpper = word.uppercase().trim()
        if (wordUpper.length < 2) {
            _currentSwipeText.value = ""
            return
        }

        val level = if (_currentScreen.value == GameScreen.DailyChallenge) _dailyLevel.value else _activeLevel.value
        if (level == null) return

        if (_currentScreen.value == GameScreen.DailyChallenge) {
            // Daily Challenge Validation
            if (level.gridWords.contains(wordUpper)) {
                if (_dailySolvedWords.value.contains(wordUpper)) {
                    showToast("Already Solved!")
                    triggerSwipeError()
                } else {
                    val updated = _dailySolvedWords.value + wordUpper
                    _dailySolvedWords.value = updated
                    showToast("Nice Word! +10 Coins", isMilestone = true)
                    viewModelScope.launch {
                        val isAllSolved = updated.size == level.gridWords.size
                        repository.saveDailyChallengeState(
                            dateStr = getTodayDateStr(),
                            solvedWords = updated.toList(),
                            isCompleted = isAllSolved,
                            rewardedCoins = isAllSolved
                        )
                        if (isAllSolved) {
                            _dailyCompleted.value = true
                            _celebrationMilestone.value = "CHALLENGE COMPLETED!"
                        }
                    }
                }
            } else if (level.bonusWords.contains(wordUpper)) {
                showToast("Extra Word! +5 Coins")
                viewModelScope.launch {
                    repository.addCoins(5)
                }
            } else {
                showToast("Invalid Word")
                triggerSwipeError()
            }
        } else {
            // Regular Level Validation
            if (level.gridWords.contains(wordUpper)) {
                if (_solvedWords.value.contains(wordUpper)) {
                    showToast("Already Solved!")
                    triggerSwipeError()
                } else {
                    val updated = _solvedWords.value + wordUpper
                    _solvedWords.value = updated
                    
                    // Show random fun educational milestones based on size
                    val celebratoryTitles = listOf("AWESOME!", "MARVELOUS!", "BRILLIANT!", "EXCELLENT!", "SUPERB!")
                    val bannerText = celebratoryTitles.random()
                    showToast(bannerText, isMilestone = true)

                    viewModelScope.launch {
                        repository.saveLevelGameplayState(
                            levelId = level.id,
                            solvedWords = updated.toList(),
                            revealedCoords = _revealedCoords.value.toList()
                        )

                        // Check Level Completion
                        val isAllSolved = updated.size == level.gridWords.size
                        if (isAllSolved) {
                            _celebrationMilestone.value = "LEVEL COMPLETE!"
                            repository.completeLevel(level.id, starsEarned = 3, coinsEarned = 25)
                            
                            // Auto transition sequence to next level after brief celebratory delay
                            kotlinx.coroutines.delay(2000)
                            val nextLevelId = level.id + 1
                            if (nextLevelId <= StaticLevelSource.levels.size) {
                                loadLevel(nextLevelId)
                            } else {
                                navigate(GameScreen.LevelSelect)
                            }
                        }
                    }
                }
            } else if (level.bonusWords.contains(wordUpper)) {
                if (_foundExtraWords.value.contains(wordUpper)) {
                    showToast("Extra Word Already Found!")
                } else {
                    val updated = _foundExtraWords.value + wordUpper
                    _foundExtraWords.value = updated
                    showToast("Extra Word! +5 Coins")
                    viewModelScope.launch {
                        repository.addCoins(5)
                    }
                }
            } else {
                showToast("Not in Word List")
                triggerSwipeError()
            }
        }

        _currentSwipeText.value = ""
    }

    private fun triggerSwipeError() {
        _swipeErrorTrigger.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _swipeErrorTrigger.value = false
        }
    }

    // Handles the standard bulb Hint option (reveals one random unrevealed crossword block)
    fun purchaseSingleHint() {
        val level = if (_currentScreen.value == GameScreen.DailyChallenge) _dailyLevel.value else _activeLevel.value
        if (level == null) return

        viewModelScope.launch {
            val stats = userStats.value
            if (stats.coins < 25) {
                showToast("Not Enough Coins (Needs 25)")
                return@launch
            }

            // Deduct coins
            val success = repository.deductCoins(25)
            if (!success) return@launch

            // Discover unrevealed cells on the active crossword grid
            val allCells = level.computeGridCells()
            val solved = if (_currentScreen.value == GameScreen.DailyChallenge) _dailySolvedWords.value else _solvedWords.value
            
            // Cells that are already solved by word completion
            val solvedCellsCoords = mutableSetOf<Pair<Int, Int>>()
            for (p in level.placements) {
                if (solved.contains(p.word)) {
                    for (i in p.word.indices) {
                        val r = if (p.isHorizontal) p.startRow else p.startRow + i
                        val c = if (p.isHorizontal) p.startCol + i else p.startCol
                        solvedCellsCoords.add(Pair(r, c))
                    }
                }
            }

            // Unrevealed coords: inside all cells but NOT solved and NOT currently hint-revealed
            val currentRevealed = if (_currentScreen.value == GameScreen.DailyChallenge) _dailyRevealedCoords.value else _revealedCoords.value
            val unrevealed = allCells.keys.filter {
                !solvedCellsCoords.contains(it) && !currentRevealed.contains(it)
            }

            if (unrevealed.isNotEmpty()) {
                val chosenCoord = unrevealed.random()
                if (_currentScreen.value == GameScreen.DailyChallenge) {
                    _dailyRevealedCoords.value = _dailyRevealedCoords.value + chosenCoord
                } else {
                    _revealedCoords.value = _revealedCoords.value + chosenCoord
                    repository.saveLevelGameplayState(
                        levelId = level.id,
                        solvedWords = _solvedWords.value.toList(),
                        revealedCoords = _revealedCoords.value.toList()
                    )
                }
                showToast("Letter Revealed!")
            } else {
                showToast("All current cells are already clear!")
                // Refund coins
                repository.addCoins(25)
            }
        }
    }

    // Handles the rocket booster option (reveals 3 letters across the grid!)
    fun purchaseRocketBooster() {
        val level = if (_currentScreen.value == GameScreen.DailyChallenge) _dailyLevel.value else _activeLevel.value
        if (level == null) return

        viewModelScope.launch {
            val stats = userStats.value
            if (stats.coins < 300) {
                showToast("Not Enough Coins (Needs 300)")
                return@launch
            }

            val success = repository.deductCoins(300)
            if (!success) return@launch

            val allCells = level.computeGridCells()
            val solved = if (_currentScreen.value == GameScreen.DailyChallenge) _dailySolvedWords.value else _solvedWords.value
            
            val solvedCellsCoords = mutableSetOf<Pair<Int, Int>>()
            for (p in level.placements) {
                if (solved.contains(p.word)) {
                    for (i in p.word.indices) {
                        val r = if (p.isHorizontal) p.startRow else p.startRow + i
                        val c = if (p.isHorizontal) p.startCol + i else p.startCol
                        solvedCellsCoords.add(Pair(r, c))
                    }
                }
            }

            val currentRevealed = if (_currentScreen.value == GameScreen.DailyChallenge) _dailyRevealedCoords.value else _revealedCoords.value
            val unrevealed = allCells.keys.filter {
                !solvedCellsCoords.contains(it) && !currentRevealed.contains(it)
            }

            if (unrevealed.isNotEmpty()) {
                // Pick up to 3 random targets
                val count = minOf(3, unrevealed.size)
                val chosen = unrevealed.shuffled().take(count)
                if (_currentScreen.value == GameScreen.DailyChallenge) {
                    _dailyRevealedCoords.value = _dailyRevealedCoords.value + chosen
                } else {
                    _revealedCoords.value = _revealedCoords.value + chosen
                    repository.saveLevelGameplayState(
                        levelId = level.id,
                        solvedWords = _solvedWords.value.toList(),
                        revealedCoords = _revealedCoords.value.toList()
                    )
                }
                showToast("Rocket Blast! $count Letters Revealed!")
            } else {
                showToast("No blocks left to reveal!")
                repository.addCoins(300)
            }
        }
    }

    fun showToast(msg: String, isMilestone: Boolean = false) {
        if (isMilestone) {
            _toastMessage.value = msg
        } else {
            _toastMessage.value = msg
            viewModelScope.launch {
                kotlinx.coroutines.delay(1800)
                if (_toastMessage.value == msg) {
                    _toastMessage.value = null
                }
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun dismissCelebration() {
        _celebrationMilestone.value = null
    }
}

// Custom Daily Challenge Level specification helper
object DailyChallengeSource {
    fun getDailyLevel(dateStr: String): LevelSpec {
        val daysOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val hash = dateStr.hashCode()
        val index = kotlin.math.abs(hash) % 7
        val dayName = daysOfWeek[index]

        return when (index) {
            0 -> LevelSpec(
                id = -99,
                themeName = "$dayName Session",
                colorStyle = "Red",
                letters = "HISTORY",
                gridWords = listOf("HISTORY", "TOYS", "HOST"),
                placements = listOf(
                    WordPlacement("HISTORY", 2, 0, true),
                    WordPlacement("TOYS", 2, 3, false),
                    WordPlacement("HOST", 2, 0, false)
                ),
                bonusWords = listOf("STORY", "SHIRT", "TOY", "SOIR", "TOSY", "HIS")
            )
            1 -> LevelSpec(
                id = -98,
                themeName = "$dayName Activity",
                colorStyle = "Orange",
                letters = "SCHOOL",
                gridWords = listOf("SCHOOL", "COOL", "SOLO"),
                placements = listOf(
                    WordPlacement("SCHOOL", 2, 0, true),
                    WordPlacement("COOL", 1, 1, false),
                    WordPlacement("SOLO", 2, 0, false)
                ),
                bonusWords = listOf("CHOS", "LOOS", "SOO", "SCHO", "COO", "SOL")
            )
            2 -> LevelSpec(
                id = -97,
                themeName = "$dayName Research",
                colorStyle = "Blue",
                letters = "CHEMIST",
                gridWords = listOf("CHEMIST", "CHIPS", "SEMI"),
                placements = listOf(
                    WordPlacement("CHEMIST", 2, 0, true),
                    WordPlacement("CHIPS", 2, 0, false),
                    WordPlacement("SEMI", 2, 3, false)
                ),
                bonusWords = listOf("MIST", "HEM", "HIM", "SHE", "ITS", "MET", "THEM")
            )
            3 -> LevelSpec(
                id = -96,
                themeName = "$dayName Hour",
                colorStyle = "Red",
                letters = "READING",
                gridWords = listOf("READING", "DEAR", "RAIN"),
                placements = listOf(
                    WordPlacement("READING", 2, 0, true),
                    WordPlacement("DEAR", 2, 3, false),
                    WordPlacement("RAIN", 2, 0, false)
                ),
                bonusWords = listOf("RANG", "DEAN", "DING", "RING", "EAR", "ERA", "ARE", "DEAR")
            )
            4 -> LevelSpec(
                id = -95,
                themeName = "$dayName Challenge",
                colorStyle = "Orange",
                letters = "ATHLETE",
                gridWords = listOf("ATHLETE", "HALE", "LATE"),
                placements = listOf(
                    WordPlacement("ATHLETE", 2, 0, true),
                    WordPlacement("HALE", 2, 2, false),
                    WordPlacement("LATE", 2, 4, false)
                ),
                bonusWords = listOf("TEAL", "HARE", "HAT", "THE", "EAT", "LET", "ALE")
            )
            5 -> LevelSpec(
                id = -93,
                themeName = "$dayName Recital",
                colorStyle = "Blue",
                letters = "SINGER",
                gridWords = listOf("SINGER", "REIGN", "SING"),
                placements = listOf(
                    WordPlacement("SINGER", 2, 0, true),
                    WordPlacement("REIGN", 2, 5, false),
                    WordPlacement("SING", 2, 0, false)
                ),
                bonusWords = listOf("RING", "RISE", "GIN", "IRE", "SER", "ENG")
            )
            else -> LevelSpec(
                id = -92,
                themeName = "$dayName Stadium",
                colorStyle = "Orange",
                letters = "PASSAGE",
                gridWords = listOf("PASSAGE", "PAGES", "PASS"),
                placements = listOf(
                    WordPlacement("PASSAGE", 2, 0, true),
                    WordPlacement("PAGES", 2, 0, false),
                    WordPlacement("PASS", 2, 4, false)
                ),
                bonusWords = listOf("GAPS", "SAGE", "GAS", "ASP", "SEA", "SAG", "APE", "PASS")
            )
        }
    }
}
