package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_stats WHERE userId = 'single_user' LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE userId = 'single_user' LIMIT 1")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Query("SELECT * FROM level_gameplay_state WHERE levelId = :levelId LIMIT 1")
    fun getGameplayStateFlow(levelId: Int): Flow<LevelGameplayState?>

    @Query("SELECT * FROM level_gameplay_state WHERE levelId = :levelId LIMIT 1")
    suspend fun getGameplayStateDirect(levelId: Int): LevelGameplayState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameplayState(state: LevelGameplayState)

    @Query("DELETE FROM level_gameplay_state WHERE levelId = :levelId")
    suspend fun clearLevelState(levelId: Int)

    @Query("SELECT * FROM leaderboard ORDER BY score DESC")
    fun getLeaderboardEntriesFlow(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)

    @Query("SELECT * FROM daily_challenge_state WHERE dateStr = :dateStr LIMIT 1")
    fun getDailyChallengeStateFlow(dateStr: String): Flow<DailyChallengeState?>

    @Query("SELECT * FROM daily_challenge_state WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDailyChallengeStateDirect(dateStr: String): DailyChallengeState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChallengeState(state: DailyChallengeState)

    @Query("DELETE FROM user_stats")
    suspend fun clearUserStats()

    @Query("DELETE FROM level_gameplay_state")
    suspend fun clearAllLevelGameplayState()

    @Query("DELETE FROM daily_challenge_state")
    suspend fun clearAllDailyChallengeState()

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}
