package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

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

    private var toastDismissJob: kotlinx.coroutines.Job? = null

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
        
        // Synchronously reset all transient gameplay state to avoid "stale" UI frames
        _wheelLetters.value = level.letters.toList().shuffled()
        _activeLevel.value = level
        _solvedWords.value = emptySet()
        _revealedCoords.value = emptySet()
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
            }
        }
    }

    // Load daily challenge
    private fun loadDailyChallenge() {
        val todayStr = getTodayDateStr()
        val level = DailyChallengeSource.getDailyLevel(todayStr)
        
        // Reset state synchronously
        _dailyWheelLetters.value = level.letters.toList().shuffled()
        _dailyLevel.value = level
        _dailySolvedWords.value = emptySet()
        _dailyRevealedCoords.value = emptySet()
        _dailyCompleted.value = false
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
            }
        }
    }

    fun restartLevel(levelId: Int) {
        viewModelScope.launch {
            repository.clearLevelGameplayState(levelId)
            loadLevel(levelId)
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.resetAllProgress()
            // After reset, navigate back home and everything will refresh via Flows
            navigate(GameScreen.Home)
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
        // Prevent processing swipes during transition/celebration
        if (_celebrationMilestone.value != null) return

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
                    showToast("Nice Word! +10 Coins")
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
                    showToast(bannerText)

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
                            delay(2000L.milliseconds)
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
            delay(500L.milliseconds)
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

    fun showToast(msg: String) {
        toastDismissJob?.cancel()
        _toastMessage.value = msg
        toastDismissJob = viewModelScope.launch {
            delay(3000L.milliseconds)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
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
                letters = "HISTORYY",
                gridWords = listOf("HISTORY", "STORY", "TOYS", "HOST", "HIS"),
                placements = listOf(
                    WordPlacement("HISTORY", 5, 0, true),
                    WordPlacement("HIS", 5, 0, false),
                    WordPlacement("STORY", 2, 2, false),
                    WordPlacement("TOYS", 8, 2, true),
                    WordPlacement("HOST", 4, 6, false)
                ),
                bonusWords = listOf("SHIRT", "TOY", "SOIR")
            )
            1 -> LevelSpec(
                id = -98,
                themeName = "$dayName Activity",
                colorStyle = "Orange",
                letters = "SCHOOLL",
                gridWords = listOf("SCHOOL", "COOL", "SOLO", "LOOS", "SOO"),
                placements = listOf(
                    WordPlacement("SCHOOL", 5, 0, true),
                    WordPlacement("SOLO", 5, 0, false),
                    WordPlacement("COOL", 2, 1, false),
                    WordPlacement("LOOS", 8, 3, false),
                    WordPlacement("SOO", 4, 3, true)
                ),
                bonusWords = listOf("CHOS", "SCHO", "SOL")
            )
            2 -> LevelSpec(
                id = -97,
                themeName = "$dayName Research",
                colorStyle = "Blue",
                letters = "CHEMISTT",
                gridWords = listOf("CHEMIST", "CHIPS", "SEMI", "MIST", "HEM"),
                placements = listOf(
                    WordPlacement("CHEMIST", 5, 0, true),
                    WordPlacement("CHIPS", 5, 0, false),
                    WordPlacement("HEM", 2, 1, false),
                    WordPlacement("SEMI", 8, 3, false),
                    WordPlacement("MIST", 4, 3, true)
                ),
                bonusWords = listOf("HIM", "SHE", "ITS", "MET")
            )
            3 -> LevelSpec(
                id = -96,
                themeName = "$dayName Hour",
                colorStyle = "Red",
                letters = "READINGG",
                gridWords = listOf("READING", "DEAR", "RAIN", "RANG", "DING"),
                placements = listOf(
                    WordPlacement("READING", 5, 0, true),
                    WordPlacement("RAIN", 5, 0, false),
                    WordPlacement("DEAR", 2, 3, false),
                    WordPlacement("RANG", 8, 0, true),
                    WordPlacement("DING", 4, 3, false)
                ),
                bonusWords = listOf("DEAN", "RING", "EAR", "ERA")
            )
            4 -> LevelSpec(
                id = -95,
                themeName = "$dayName Challenge",
                colorStyle = "Orange",
                letters = "ATHLETEE",
                gridWords = listOf("ATHLETE", "HALE", "LATE", "TEAL", "ALE"),
                placements = listOf(
                    WordPlacement("ATHLETE", 5, 0, true),
                    WordPlacement("ALE", 5, 0, false),
                    WordPlacement("HALE", 2, 2, false),
                    WordPlacement("LATE", 8, 4, false),
                    WordPlacement("TEAL", 4, 4, true)
                ),
                bonusWords = listOf("HAT", "THE", "EAT", "LET")
            )
            5 -> LevelSpec(
                id = -93,
                themeName = "$dayName Recital",
                colorStyle = "Blue",
                letters = "SINGERR",
                gridWords = listOf("SINGER", "REIGN", "SING", "RING", "RISE"),
                placements = listOf(
                    WordPlacement("SINGER", 5, 0, true),
                    WordPlacement("SING", 5, 0, false),
                    WordPlacement("REIGN", 2, 4, false),
                    WordPlacement("RING", 8, 5, false),
                    WordPlacement("RISE", 4, 0, true)
                ),
                bonusWords = listOf("GIN", "IRE", "SER", "ENG")
            )
            else -> LevelSpec(
                id = -92,
                themeName = "$dayName Stadium",
                colorStyle = "Orange",
                letters = "PASSAGEE",
                gridWords = listOf("PASSAGE", "PAGES", "PASS", "GAPS", "SAGE"),
                placements = listOf(
                    WordPlacement("PASSAGE", 5, 0, true),
                    WordPlacement("PASS", 5, 0, false),
                    WordPlacement("PAGES", 2, 1, false),
                    WordPlacement("SAGE", 8, 3, false),
                    WordPlacement("GAPS", 4, 0, true)
                ),
                bonusWords = listOf("GAS", "ASP", "SEA", "SAG")
            )
        }
    }
}
