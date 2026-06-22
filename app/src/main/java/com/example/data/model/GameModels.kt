package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class WordPlacement(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val isHorizontal: Boolean
)

data class LevelSpec(
    val id: Int,
    val themeName: String,
    val colorStyle: String, // "Orange", "Blue", "Red"
    val letters: String,    // e.g. "LEARN"
    val gridWords: List<String>,
    val placements: List<WordPlacement>,
    val bonusWords: List<String>
) {
    // Computes all occupied cells and their expected characters
    fun computeGridCells(): Map<Pair<Int, Int>, Char> {
        val cells = mutableMapOf<Pair<Int, Int>, Char>()
        for (p in placements) {
            for (i in p.word.indices) {
                val r = if (p.isHorizontal) p.startRow else p.startRow + i
                val c = if (p.isHorizontal) p.startCol + i else p.startCol
                cells[Pair(r, c)] = p.word[i]
            }
        }
        return cells
    }
}

// User statistics stored in local database
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val userId: String = "single_user",
    val currentLevelId: Int = 1,
    val coins: Int = 300, // Enriched starting coins so users can test hints/boosters!
    val stars: Int = 0,
    val completedChallengesCount: Int = 0,
    val lastDailyChallengeDate: String = "",
    val playerName: String = "",
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

// Persists current game progress for seamless state preservation across launches
@Entity(tableName = "level_gameplay_state")
data class LevelGameplayState(
    @PrimaryKey val levelId: Int,
    val solvedWordsSeparated: String, // e.g. "LEARN,LANE"
    val revealedCoordsSeparated: String // e.g. "2,3;2,4" for hints
)

// Holds list of users for leaderboard
@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val username: String,
    val score: Int,
    val avatarColorHex: String,
    val isCurrentUser: Boolean = false
)

// Daily challenge tracking
@Entity(tableName = "daily_challenge_state")
data class DailyChallengeState(
    @PrimaryKey val dateStr: String, // e.g. "2026-06-22"
    val solvedWordsSeparated: String,
    val isCompleted: Boolean = false,
    val rewardedCoins: Boolean = false
)

object StaticLevelSource {
    val levels = listOf(
        LevelSpec(
            id = 1,
            themeName = "Classroom",
            colorStyle = "Red",
            letters = "LEARN",
            gridWords = listOf("LEARN", "LANE", "REAL"),
            placements = listOf(
                WordPlacement("LEARN", 2, 0, true),
                WordPlacement("LANE", 0, 4, false),
                WordPlacement("REAL", 2, 3, false)
            ),
            bonusWords = listOf("NEAR", "EAR", "ERA", "ARE", "RAN", "ALE", "LEAN")
        ),
        LevelSpec(
            id = 2,
            themeName = "Courtyard",
            colorStyle = "Orange",
            letters = "DEAR",
            gridWords = listOf("READ", "DEAR", "RED"),
            placements = listOf(
                WordPlacement("READ", 1, 0, true),
                WordPlacement("DEAR", 1, 3, false),
                WordPlacement("RED", 0, 1, false)
            ),
            bonusWords = listOf("DARE", "EAR", "ERA", "ARE", "RAD")
        ),
        LevelSpec(
            id = 3,
            themeName = "Cafeteria",
            colorStyle = "Blue",
            letters = "STUDY",
            gridWords = listOf("STUDY", "DUST", "STUD"),
            placements = listOf(
                WordPlacement("STUDY", 2, 0, true),
                WordPlacement("DUST", 2, 3, false),
                WordPlacement("STUD", 2, 0, false)
            ),
            bonusWords = listOf("DUTY", "STY", "YUT", "SUD")
        ),
        LevelSpec(
            id = 4,
            themeName = "Library",
            colorStyle = "Blue",
            letters = "BOOKS",
            gridWords = listOf("BOOKS", "BOOK", "SOB"),
            placements = listOf(
                WordPlacement("BOOKS", 2, 0, true),
                WordPlacement("BOOK", 2, 0, false),
                WordPlacement("SOB", 2, 4, false)
            ),
            bonusWords = listOf("BOOS", "BOS", "KOB")
        ),
        LevelSpec(
            id = 5,
            themeName = "Science Lab",
            colorStyle = "Red",
            letters = "WRITE",
            gridWords = listOf("WRITE", "WIRE", "TIRE"),
            placements = listOf(
                WordPlacement("WRITE", 2, 0, true),
                WordPlacement("WIRE", 2, 0, false),
                WordPlacement("TIRE", 2, 3, false)
            ),
            bonusWords = listOf("RITE", "WET", "TIE", "WIT", "WRIT")
        ),
        LevelSpec(
            id = 6,
            themeName = "Gymnasium",
            colorStyle = "Orange",
            letters = "SPORT",
            gridWords = listOf("SPORT", "PORTS", "ROT"),
            placements = listOf(
                WordPlacement("SPORT", 2, 0, true),
                WordPlacement("PORTS", 2, 1, false),
                WordPlacement("ROT", 2, 3, false)
            ),
            bonusWords = listOf("SPOR", "TOP", "POT", "SOPS", "PROS", "SORT", "STOP")
        ),
        LevelSpec(
            id = 7,
            themeName = "Auditorium",
            colorStyle = "Blue",
            letters = "CLASS",
            gridWords = listOf("CLASS", "LASS", "SAC"),
            placements = listOf(
                WordPlacement("CLASS", 2, 0, true),
                WordPlacement("LASS", 2, 1, false),
                WordPlacement("SAC", 2, 3, false)
            ),
            bonusWords = listOf("ALAS", "LAC", "SAL")
        ),
        LevelSpec(
            id = 8,
            themeName = "Playground",
            colorStyle = "Red",
            letters = "GRADE",
            gridWords = listOf("GRADE", "DEAR", "RAG"),
            placements = listOf(
                WordPlacement("GRADE", 2, 0, true),
                WordPlacement("DEAR", 2, 3, false),
                WordPlacement("RAG", 2, 1, false)
            ),
            bonusWords = listOf("DARE", "GEAR", "READ", "RED", "ERA", "ARE", "DAG")
        ),
        LevelSpec(
            id = 9,
            themeName = "Principal Office",
            colorStyle = "Orange",
            letters = "TEACH",
            gridWords = listOf("TEACH", "CHAT", "HATE"),
            placements = listOf(
                WordPlacement("TEACH", 2, 0, true),
                WordPlacement("CHAT", 2, 3, false),
                WordPlacement("HATE", 2, 4, false)
            ),
            bonusWords = listOf("EACH", "HEAT", "TEA", "CAT", "HAT", "ACE")
        ),
        LevelSpec(
            id = 10,
            themeName = "Dormitory",
            colorStyle = "Blue",
            letters = "STUDENT",
            gridWords = listOf("STUDENT", "DENT", "SEND"),
            placements = listOf(
                WordPlacement("STUDENT", 2, 0, true),
                WordPlacement("DENT", 2, 3, false),
                WordPlacement("SEND", 2, 0, false)
            ),
            bonusWords = listOf("NEST", "TENT", "STUNT", "DUST", "STUDS", "NUT", "TEN", "NET")
        ),
        LevelSpec(
            id = 11,
            themeName = "Art Studio",
            colorStyle = "Orange",
            letters = "PAINT",
            gridWords = listOf("PAINT", "PIN", "TIN"),
            placements = listOf(
                WordPlacement("PAINT", 2, 0, true),
                WordPlacement("PIN", 2, 0, false),
                WordPlacement("TIN", 2, 4, false)
            ),
            bonusWords = listOf("PANT", "PAIN", "NIP", "TAN", "PAN", "PAT", "TAP")
        ),
        LevelSpec(
            id = 12,
            themeName = "Computer Lab",
            colorStyle = "Red",
            letters = "CODING",
            gridWords = listOf("CODING", "DOG", "GIN"),
            placements = listOf(
                WordPlacement("CODING", 2, 0, true),
                WordPlacement("DOG", 2, 2, false),
                WordPlacement("GIN", 2, 5, false)
            ),
            bonusWords = listOf("COIN", "DING", "ION", "GOD", "COD", "DICING", "CON")
        )
    )
}
