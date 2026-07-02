package com.example.data.repository

import com.example.data.database.UserProgressDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: UserProgressDao) {
    val userStatsFlow: Flow<UserStats> = dao.getUserStatsFlow().map { it ?: UserStats() }
    val leaderboardEntriesFlow: Flow<List<LeaderboardEntry>> = dao.getLeaderboardEntriesFlow()

    fun getLevelGameplayStateFlow(levelId: Int): Flow<LevelGameplayState?> =
        dao.getGameplayStateFlow(levelId)

    suspend fun getLevelGameplayStateDirect(levelId: Int): LevelGameplayState? =
        dao.getGameplayStateDirect(levelId)

    suspend fun saveLevelGameplayState(levelId: Int, solvedWords: List<String>, revealedCoords: List<Pair<Int, Int>>) {
        val solvedJson = solvedWords.joinToString(",")
        val coordsJson = revealedCoords.joinToString(";") { "${it.first},${it.second}" }
        val state = LevelGameplayState(levelId, solvedJson, coordsJson)
        dao.insertGameplayState(state)
    }

    suspend fun clearLevelGameplayState(levelId: Int) {
        dao.clearLevelState(levelId)
    }

    suspend fun completeLevel(levelId: Int, starsEarned: Int, coinsEarned: Int) {
        val currentStats = dao.getUserStatsDirect() ?: UserStats()
        val nextLevelId = if (levelId == currentStats.currentLevelId) {
            val maxLevel = StaticLevelSource.levels.size
            if (currentStats.currentLevelId < maxLevel) currentStats.currentLevelId + 1 else currentStats.currentLevelId
        } else {
            currentStats.currentLevelId
        }

        val updatedStats = currentStats.copy(
            currentLevelId = nextLevelId,
            stars = currentStats.stars + starsEarned,
            coins = currentStats.coins + coinsEarned
        )
        dao.insertUserStats(updatedStats)
        dao.clearLevelState(levelId)

        updateLeaderboardScore(updatedStats.stars, updatedStats.completedChallengesCount)
    }

    suspend fun addCoins(amount: Int) {
        val stats = dao.getUserStatsDirect() ?: UserStats()
        dao.insertUserStats(stats.copy(coins = stats.coins + amount))
    }

    suspend fun updatePlayerName(name: String) {
        val stats = dao.getUserStatsDirect() ?: UserStats()
        dao.insertUserStats(stats.copy(playerName = name))
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        val stats = dao.getUserStatsDirect() ?: UserStats()
        dao.insertUserStats(stats.copy(soundEnabled = enabled))
    }

    suspend fun updateHapticEnabled(enabled: Boolean) {
        val stats = dao.getUserStatsDirect() ?: UserStats()
        dao.insertUserStats(stats.copy(hapticEnabled = enabled))
    }

    suspend fun deductCoins(amount: Int): Boolean {
        val stats = dao.getUserStatsDirect() ?: UserStats()
        if (stats.coins >= amount) {
            dao.insertUserStats(stats.copy(coins = stats.coins - amount))
            return true
        }
        return false
    }

    private suspend fun updateLeaderboardScore(stars: Int, challenges: Int) {
        val newScore = (stars * 15) + (challenges * 100)
        dao.insertLeaderboard(listOf(LeaderboardEntry("You", newScore, "#2196F3", isCurrentUser = true)))
    }

    fun getDailyChallengeStateFlow(dateStr: String): Flow<DailyChallengeState?> =
        dao.getDailyChallengeStateFlow(dateStr)

    suspend fun getDailyChallengeStateDirect(dateStr: String): DailyChallengeState? =
        dao.getDailyChallengeStateDirect(dateStr)

    suspend fun resetAllProgress() {
        dao.clearUserStats()
        dao.clearAllLevelGameplayState()
        dao.clearAllDailyChallengeState()
        dao.clearLeaderboard()
    }

    suspend fun saveDailyChallengeState(dateStr: String, solvedWords: List<String>, isCompleted: Boolean, rewardedCoins: Boolean) {
        val state = DailyChallengeState(
            dateStr = dateStr,
            solvedWordsSeparated = solvedWords.joinToString(","),
            isCompleted = isCompleted,
            rewardedCoins = rewardedCoins
        )
        dao.insertDailyChallengeState(state)

        if (isCompleted) {
            val stats = dao.getUserStatsDirect() ?: UserStats()
            if (stats.lastDailyChallengeDate != dateStr) {
                val updated = stats.copy(
                    completedChallengesCount = stats.completedChallengesCount + 1,
                    lastDailyChallengeDate = dateStr,
                    coins = stats.coins + (if (!rewardedCoins) 100 else 0)
                )
                dao.insertUserStats(updated)
                
                val finalizedState = state.copy(rewardedCoins = true)
                dao.insertDailyChallengeState(finalizedState)

                updateLeaderboardScore(updated.stars, updated.completedChallengesCount)
            }
        }
    }
}
