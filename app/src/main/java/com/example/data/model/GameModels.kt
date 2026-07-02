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
    val coins: Int = 300,
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
            gridWords = listOf("LEARN", "LEAN", "REAL", "LANE", "NEAR"),
            placements = listOf(
                WordPlacement("LEARN", 5, 0, true),
                WordPlacement("LEAN", 5, 0, false),
                WordPlacement("LANE", 4, 2, false),
                WordPlacement("REAL", 5, 3, false),
                WordPlacement("NEAR", 5, 4, false)
            ),
            bonusWords = listOf("EAR", "ERA", "ARE", "RAN", "ALE")
        ),
        LevelSpec(
            id = 2,
            themeName = "Courtyard",
            colorStyle = "Orange",
            letters = "GRADE",
            gridWords = listOf("GRADE", "DARE", "READ", "GEAR", "DEAR"),
            placements = listOf(
                WordPlacement("GRADE", 5, 0, true),
                WordPlacement("GEAR", 5, 0, false),
                WordPlacement("READ", 5, 1, false),
                WordPlacement("DARE", 4, 2, false),
                WordPlacement("DEAR", 5, 3, false)
            ),
            bonusWords = listOf("RAG", "RED", "ERA", "ARE")
        ),
        LevelSpec(
            id = 3,
            themeName = "Cafeteria",
            colorStyle = "Blue",
            letters = "STUDENT",
            gridWords = listOf("STUDENT", "DENT", "TENT", "SENT", "NET"),
            placements = listOf(
                WordPlacement("STUDENT", 5, 0, true),
                WordPlacement("DENT", 5, 3, false),
                WordPlacement("SENT", 5, 0, false),
                WordPlacement("TENT", 5, 6, false),
                WordPlacement("NET", 5, 5, false)
            ),
            bonusWords = listOf("STUNT", "DUST", "TEN")
        ),
        LevelSpec(
            id = 4,
            themeName = "Library",
            colorStyle = "Blue",
            letters = "BOOKSS",
            gridWords = listOf("BOOKS", "BOOK", "SOB", "BOOS", "BOSS"),
            placements = listOf(
                WordPlacement("BOOKS", 5, 0, true),
                WordPlacement("BOOK", 5, 0, false),
                WordPlacement("BOOS", 4, 1, false),
                WordPlacement("SOB", 4, 2, false),
                WordPlacement("BOSS", 2, 4, false)
            ),
            bonusWords = listOf("KOB")
        ),
        LevelSpec(
            id = 5,
            themeName = "Science Lab",
            colorStyle = "Red",
            letters = "WRITE",
            gridWords = listOf("WRITE", "WIRE", "TIRE", "RITE", "WRIT"),
            placements = listOf(
                WordPlacement("WRITE", 5, 0, true),
                WordPlacement("WRIT", 5, 0, false),
                WordPlacement("RITE", 5, 1, false),
                WordPlacement("WIRE", 4, 2, false),
                WordPlacement("TIRE", 5, 3, false)
            ),
            bonusWords = listOf("WET", "TIE", "WIT")
        ),
        LevelSpec(
            id = 6,
            themeName = "Gymnasium",
            colorStyle = "Orange",
            letters = "SPORT",
            gridWords = listOf("SPORT", "PORTS", "ROT", "STOP", "SORT"),
            placements = listOf(
                WordPlacement("SPORT", 5, 0, true),
                WordPlacement("SORT", 5, 0, false),
                WordPlacement("PORTS", 5, 1, false),
                WordPlacement("STOP", 3, 2, false),
                WordPlacement("ROT", 5, 3, false)
            ),
            bonusWords = listOf("TOP", "POT", "PROS")
        ),
        LevelSpec(
            id = 7,
            themeName = "Auditorium",
            colorStyle = "Blue",
            letters = "CLASSA",
            gridWords = listOf("CLASS", "LASS", "SAC", "ALAS", "SAL"),
            placements = listOf(
                WordPlacement("CLASS", 5, 0, true),
                WordPlacement("LASS", 5, 1, false),
                WordPlacement("SAC", 5, 3, false),
                WordPlacement("ALAS", 3, 2, false),
                WordPlacement("SAL", 5, 4, false)
            ),
            bonusWords = listOf("LAC")
        ),
        LevelSpec(
            id = 8,
            themeName = "Art Studio",
            colorStyle = "Orange",
            letters = "PAINT",
            gridWords = listOf("PAINT", "PIN", "TIN", "PANT", "PAIN"),
            placements = listOf(
                WordPlacement("PAINT", 5, 0, true),
                WordPlacement("PAIN", 5, 0, false),
                WordPlacement("PANT", 2, 4, false),
                WordPlacement("PIN", 4, 2, false),
                WordPlacement("TIN", 3, 3, false)
            ),
            bonusWords = listOf("NIP", "TAN", "PAN")
        ),
        LevelSpec(
            id = 9,
            themeName = "IT Desk",
            colorStyle = "Red",
            letters = "CODING",
            gridWords = listOf("CODING", "DOG", "GIN", "COIN", "DING"),
            placements = listOf(
                WordPlacement("CODING", 5, 0, true),
                WordPlacement("COIN", 5, 0, false),
                WordPlacement("DOG", 5, 2, false),
                WordPlacement("DING", 2, 5, false),
                WordPlacement("GIN", 3, 4, false)
            ),
            bonusWords = listOf("ION", "GOD", "COD")
        ),
        LevelSpec(
            id = 10,
            themeName = "Music Room",
            colorStyle = "Blue",
            letters = "GUITAR",
            gridWords = listOf("GUITAR", "AIR", "RAG", "TAG", "ART"),
            placements = listOf(
                WordPlacement("GUITAR", 5, 0, true),
                WordPlacement("TAG", 3, 0, false),
                WordPlacement("AIR", 5, 4, false),
                WordPlacement("RAG", 5, 5, false),
                WordPlacement("ART", 3, 3, false)
            ),
            bonusWords = listOf("RAT", "GUT", "TAR")
        ),
        LevelSpec(
            id = 11,
            themeName = "History Club",
            colorStyle = "Orange",
            letters = "ANCIENT",
            gridWords = listOf("ANCIENT", "TEN", "NET", "CAN", "TEA"),
            placements = listOf(
                WordPlacement("ANCIENT", 5, 0, true),
                WordPlacement("NET", 5, 1, false),
                WordPlacement("CAN", 5, 2, false),
                WordPlacement("TEN", 5, 6, false),
                WordPlacement("TEA", 3, 0, false)
            ),
            bonusWords = listOf("ANT", "TIE", "ICE")
        ),
        LevelSpec(
            id = 12,
            themeName = "Math Dept",
            colorStyle = "Blue",
            letters = "ALGEBRA",
            gridWords = listOf("ALGEBRA", "GEAR", "BEAR", "REAL", "ARE"),
            placements = listOf(
                WordPlacement("ALGEBRA", 5, 0, true),
                WordPlacement("GEAR", 5, 2, false),
                WordPlacement("BEAR", 5, 4, false),
                WordPlacement("REAL", 5, 5, false),
                WordPlacement("ARE", 5, 6, false)
            ),
            bonusWords = listOf("BAG", "ALE", "ERA")
        ),
        LevelSpec(
            id = 13,
            themeName = "Physics Lab",
            colorStyle = "Red",
            letters = "ENERGYG",
            gridWords = listOf("ENERGY", "GREY", "GENE", "EYE", "EGG"),
            placements = listOf(
                WordPlacement("ENERGY", 5, 0, true),
                WordPlacement("GENE", 5, 4, false),
                WordPlacement("GREY", 2, 5, false),
                WordPlacement("EYE", 5, 0, false),
                WordPlacement("EGG", 5, 2, false)
            ),
            bonusWords = listOf("GYRE", "ERE")
        ),
        LevelSpec(
            id = 14,
            themeName = "Bio Garden",
            colorStyle = "Orange",
            letters = "PLANTS",
            gridWords = listOf("PLANTS", "PLAN", "SALT", "PANT", "ANT"),
            placements = listOf(
                WordPlacement("PLANTS", 5, 0, true),
                WordPlacement("PLAN", 5, 0, false),
                WordPlacement("PANT", 3, 3, false),
                WordPlacement("ANT", 5, 2, false),
                WordPlacement("SALT", 5, 5, false)
            ),
            bonusWords = listOf("SLAP", "SNAP", "TAN")
        ),
        LevelSpec(
            id = 15,
            themeName = "Chemistry Hall",
            colorStyle = "Blue",
            letters = "ATOMS",
            gridWords = listOf("ATOMS", "ATOM", "MOST", "MAT", "SAT"),
            placements = listOf(
                WordPlacement("ATOMS", 5, 0, true),
                WordPlacement("ATOM", 5, 0, false),
                WordPlacement("MAT", 3, 1, false),
                WordPlacement("MOST", 5, 3, false),
                WordPlacement("SAT", 5, 4, false)
            ),
            bonusWords = listOf("TAM", "MOT")
        ),
        LevelSpec(
            id = 16,
            themeName = "Geography",
            colorStyle = "Red",
            letters = "WORLD",
            gridWords = listOf("WORLD", "WORD", "LORD", "OLD", "LOW"),
            placements = listOf(
                WordPlacement("WORLD", 5, 0, true),
                WordPlacement("WORD", 5, 0, false),
                WordPlacement("LORD", 5, 3, false),
                WordPlacement("OLD", 5, 1, false),
                WordPlacement("LOW", 6, 2, true)
            ),
            bonusWords = listOf("ROW", "ROD")
        ),
        LevelSpec(
            id = 17,
            themeName = "Ethics Room",
            colorStyle = "Orange",
            letters = "VALUES",
            gridWords = listOf("VALUES", "VALUE", "SALE", "USE", "AVE"),
            placements = listOf(
                WordPlacement("VALUES", 5, 0, true),
                WordPlacement("VALUE", 5, 0, false),
                WordPlacement("AVE", 5, 1, false),
                WordPlacement("USE", 5, 3, false),
                WordPlacement("SALE", 5, 5, false)
            ),
            bonusWords = listOf("VASE", "LAVE")
        ),
        LevelSpec(
            id = 18,
            themeName = "Language Lab",
            colorStyle = "Blue",
            letters = "SPEAKC",
            gridWords = listOf("SPEAK", "PEAK", "CAPE", "SEA", "APE"),
            placements = listOf(
                WordPlacement("SPEAK", 5, 0, true),
                WordPlacement("SEA", 3, 3, false),
                WordPlacement("PEAK", 5, 1, false),
                WordPlacement("APE", 7, 1, true),
                WordPlacement("CAPE", 2, 2, false)
            ),
            bonusWords = listOf("SAKE", "PEAS")
        ),
        LevelSpec(
            id = 19,
            themeName = "Logic Hub",
            colorStyle = "Red",
            letters = "TRUTH",
            gridWords = listOf("TRUTH", "HURT", "THRU", "HUT", "RUT"),
            placements = listOf(
                WordPlacement("TRUTH", 5, 0, true),
                WordPlacement("THRU", 5, 0, false),
                WordPlacement("RUT", 5, 1, false),
                WordPlacement("HURT", 5, 4, false),
                WordPlacement("HUT", 3, 3, false)
            ),
            bonusWords = listOf("TUT")
        ),
        LevelSpec(
            id = 20,
            themeName = "Media Center",
            colorStyle = "Orange",
            letters = "RADIOO",
            gridWords = listOf("RADIO", "ROAD", "RAID", "AIR", "ROD"),
            placements = listOf(
                WordPlacement("RADIO", 5, 0, true),
                WordPlacement("ROAD", 5, 0, false),
                WordPlacement("ROD", 4, 4, false),
                WordPlacement("RAID", 2, 2, false),
                WordPlacement("AIR", 5, 1, false)
            ),
            bonusWords = listOf("DOOR")
        ),
        LevelSpec(
            id = 21,
            themeName = "Workshop",
            colorStyle = "Blue",
            letters = "CRAFT",
            gridWords = listOf("CRAFT", "FACT", "RAFT", "ART", "CAT"),
            placements = listOf(
                WordPlacement("CRAFT", 5, 0, true),
                WordPlacement("CAT", 5, 0, false),
                WordPlacement("RAFT", 5, 1, false),
                WordPlacement("ART", 5, 2, false),
                WordPlacement("FACT", 5, 3, false)
            ),
            bonusWords = listOf("ARC", "FAR")
        ),
        LevelSpec(
            id = 22,
            themeName = "Debate Club",
            colorStyle = "Red",
            letters = "VOICED",
            gridWords = listOf("VOICE", "VICE", "COVE", "ICE", "VIE"),
            placements = listOf(
                WordPlacement("VOICE", 5, 0, true),
                WordPlacement("VIE", 5, 0, false),
                WordPlacement("VICE", 2, 4, false),
                WordPlacement("ICE", 5, 2, false),
                WordPlacement("COVE", 5, 3, false)
            ),
            bonusWords = listOf("VOID")
        ),
        LevelSpec(
            id = 23,
            themeName = "Student Union",
            colorStyle = "Orange",
            letters = "PARTY",
            gridWords = listOf("PARTY", "PART", "TRAY", "ART", "PAY"),
            placements = listOf(
                WordPlacement("PARTY", 5, 0, true),
                WordPlacement("PAY", 3, 4, false),
                WordPlacement("PART", 5, 0, false),
                WordPlacement("ART", 5, 1, false),
                WordPlacement("TRAY", 5, 3, false)
            ),
            bonusWords = listOf("RAP", "TAR")
        ),
        LevelSpec(
            id = 24,
            themeName = "Research Lab",
            colorStyle = "Blue",
            letters = "PAPER",
            gridWords = listOf("PAPER", "REAP", "PARE", "EAR", "PEA"),
            placements = listOf(
                WordPlacement("PAPER", 5, 0, true),
                WordPlacement("PEA", 5, 0, false),
                WordPlacement("PARE", 5, 2, false),
                WordPlacement("ARE", 5, 1, false),
                WordPlacement("REAP", 5, 4, false)
            ),
            bonusWords = listOf("REP", "ARE")
        ),
        LevelSpec(
            id = 25,
            themeName = "Theater Lab",
            colorStyle = "Red",
            letters = "DRAMA",
            gridWords = listOf("DRAMA", "DAM", "ARM", "RAD", "MAD"),
            placements = listOf(
                WordPlacement("DRAMA", 5, 0, true),
                WordPlacement("DAM", 5, 0, false),
                WordPlacement("MAD", 5, 3, false),
                WordPlacement("RAD", 5, 1, false),
                WordPlacement("ARM", 5, 2, false)
            ),
            bonusWords = listOf("RAM", "MAR")
        ),
        LevelSpec(
            id = 26,
            themeName = "Campus Park",
            colorStyle = "Orange",
            letters = "TREES",
            gridWords = listOf("TREES", "TREE", "SEE", "TEE", "STEER"),
            placements = listOf(
                WordPlacement("TREES", 5, 0, true),
                WordPlacement("TREE", 5, 0, false),
                WordPlacement("TEE", 4, 3, false),
                WordPlacement("SEE", 3, 2, false),
                WordPlacement("STEER", 5, 4, false)
            ),
            bonusWords = listOf("REST", "ERE")
        ),
        LevelSpec(
            id = 27,
            themeName = "Exam Hall",
            colorStyle = "Blue",
            letters = "STRESSTLT",
            gridWords = listOf("STRESS", "REST", "SETS", "LESS", "SET"),
            placements = listOf(
                WordPlacement("STRESS", 5, 0, true),
                WordPlacement("SET", 5, 0, false),
                WordPlacement("REST", 5, 2, false),
                WordPlacement("SETS", 5, 4, false),
                WordPlacement("LESS", 2, 5, false)
            ),
            bonusWords = listOf("TEST", "RES")
        ),
        LevelSpec(
            id = 28,
            themeName = "Counseling Room",
            colorStyle = "Red",
            letters = "TRUSTE",
            gridWords = listOf("TRUST", "RUST", "TRUE", "USE", "RUT"),
            placements = listOf(
                WordPlacement("TRUST", 5, 0, true),
                WordPlacement("TRUE", 5, 0, false),
                WordPlacement("RUT", 5, 1, false),
                WordPlacement("USE", 5, 2, false),
                WordPlacement("RUST", 2, 4, false)
            ),
            bonusWords = listOf("TUT")
        ),
        LevelSpec(
            id = 29,
            themeName = "Alumni Hall",
            colorStyle = "Orange",
            letters = "PRIDE",
            gridWords = listOf("PRIDE", "RIDE", "PIER", "DIE", "RED"),
            placements = listOf(
                WordPlacement("PRIDE", 5, 0, true),
                WordPlacement("PIER", 5, 0, false),
                WordPlacement("DIE", 5, 3, false),
                WordPlacement("RIDE", 5, 1, false),
                WordPlacement("RED", 4, 4, false)
            ),
            bonusWords = listOf("RIP", "IRE")
        ),
        LevelSpec(
            id = 30,
            themeName = "Admin Block",
            colorStyle = "Blue",
            letters = "OFFICE",
            gridWords = listOf("OFFICE", "OFF", "ICE", "FOE", "COIF"),
            placements = listOf(
                WordPlacement("OFFICE", 5, 0, true),
                WordPlacement("OFF", 5, 0, false),
                WordPlacement("FOE", 5, 2, false),
                WordPlacement("ICE", 3, 5, false),
                WordPlacement("COIF", 5, 4, false)
            ),
            bonusWords = listOf("COFF")
        ),
        LevelSpec(
            id = 31,
            themeName = "Archived Depo",
            colorStyle = "Red",
            letters = "BOOKSS",
            gridWords = listOf("BOOKS", "BOOK", "SOB", "BOOS", "BOSS"),
            placements = listOf(
                WordPlacement("BOOKS", 5, 0, true),
                WordPlacement("BOOK", 5, 0, false),
                WordPlacement("BOOS", 4, 1, false),
                WordPlacement("SOB", 4, 2, false),
                WordPlacement("BOSS", 2, 4, false)
            ),
            bonusWords = listOf("KOB")
        ),
        LevelSpec(
            id = 32,
            themeName = "Startup Hub",
            colorStyle = "Orange",
            letters = "AIDED",
            gridWords = listOf("IDEA", "DIE", "AID", "AIDED", "DEI"),
            placements = listOf(
                WordPlacement("IDEA", 5, 0, true),
                WordPlacement("DEI", 5, 1, false),
                WordPlacement("AIDED", 5, 3, false),
                WordPlacement("AID", 4, 1, true),
                WordPlacement("DIE", 3, 2, false)
            ),
            bonusWords = listOf("ADED")
        ),
        LevelSpec(
            id = 33,
            themeName = "Yoga Center",
            colorStyle = "Blue",
            letters = "PEACE",
            gridWords = listOf("PEACE", "CAPE", "PACE", "ACE", "PEA"),
            placements = listOf(
                WordPlacement("PEACE", 5, 0, true),
                WordPlacement("PEA", 5, 0, false),
                WordPlacement("ACE", 5, 2, false),
                WordPlacement("CAPE", 5, 3, false),
                WordPlacement("PACE", 2, 4, false)
            ),
            bonusWords = listOf("CAP")
        ),
        LevelSpec(
            id = 34,
            themeName = "Coffee Shop",
            colorStyle = "Red",
            letters = "BEANS",
            gridWords = listOf("BEANS", "BEAN", "BANE", "SEA", "BAN"),
            placements = listOf(
                WordPlacement("BEANS", 5, 0, true),
                WordPlacement("BAN", 5, 0, false),
                WordPlacement("SEA", 5, 4, false),
                WordPlacement("BEAN", 2, 3, false),
                WordPlacement("BANE", 2, 1, false)
            ),
            bonusWords = listOf("NAB", "ABS")
        ),
        LevelSpec(
            id = 35,
            themeName = "Green Roof",
            colorStyle = "Orange",
            letters = "SOLAR",
            gridWords = listOf("SOLAR", "ORAL", "SOAR", "ALSO", "SOL"),
            placements = listOf(
                WordPlacement("SOLAR", 5, 0, true),
                WordPlacement("SOL", 5, 0, false),
                WordPlacement("ALSO", 5, 3, false),
                WordPlacement("ORAL", 5, 1, false),
                WordPlacement("SOAR", 2, 4, false)
            ),
            bonusWords = listOf("LAR")
        ),
        LevelSpec(
            id = 36,
            themeName = "IT Desk",
            colorStyle = "Blue",
            letters = "RESET",
            gridWords = listOf("RESET", "REST", "TREE", "TEE", "SET"),
            placements = listOf(
                WordPlacement("RESET", 5, 0, true),
                WordPlacement("REST", 5, 0, false),
                WordPlacement("SET", 5, 2, false),
                WordPlacement("TREE", 5, 4, false),
                WordPlacement("TEE", 4, 1, false)
            ),
            bonusWords = listOf("ERE")
        ),
        LevelSpec(
            id = 37,
            themeName = "Storage Room",
            colorStyle = "Red",
            letters = "STUFFYDOA",
            gridWords = listOf("STUFF", "STUDY", "DUST", "STAY", "OFF"),
            placements = listOf(
                WordPlacement("STUFF", 5, 0, true),
                WordPlacement("STAY", 5, 0, false),
                WordPlacement("STUDY", 4, 1, false),
                WordPlacement("DUST", 4, 2, false),
                WordPlacement("OFF", 3, 4, false)
            ),
            bonusWords = listOf("UTF")
        ),
        LevelSpec(
            id = 38,
            themeName = "Study Corner",
            colorStyle = "Orange",
            letters = "QUIETA",
            gridWords = listOf("QUIET", "QUITE", "QUIT", "TIE", "TEA"),
            placements = listOf(
                WordPlacement("QUIET", 5, 0, true),
                WordPlacement("QUIT", 5, 0, false),
                WordPlacement("QUITE", 3, 2, false),
                WordPlacement("TIE", 3, 3, false),
                WordPlacement("TEA", 3, 3, true)
            ),
            bonusWords = listOf("TEI")
        ),
        LevelSpec(
            id = 39,
            themeName = "Main Gate",
            colorStyle = "Blue",
            letters = "ENTRY",
            gridWords = listOf("ENTRY", "TREN", "YEN", "TEN", "NET"),
            placements = listOf(
                WordPlacement("ENTRY", 5, 0, true),
                WordPlacement("TEN", 5, 2, false),
                WordPlacement("YEN", 5, 4, false),
                WordPlacement("NET", 5, 1, false),
                WordPlacement("TREN", 4, 3, false)
            ),
            bonusWords = listOf("TRY")
        ),
        LevelSpec(
            id = 40,
            themeName = "Archive Room",
            colorStyle = "Red",
            letters = "FILES",
            gridWords = listOf("FILES", "FILE", "LIFE", "SELF", "LIE"),
            placements = listOf(
                WordPlacement("FILES", 5, 0, true),
                WordPlacement("FILE", 5, 0, false),
                WordPlacement("LIE", 3, 3, false),
                WordPlacement("LIFE", 5, 2, false),
                WordPlacement("SELF", 5, 4, false)
            ),
            bonusWords = listOf("FIL")
        ),
        LevelSpec(
            id = 41,
            themeName = "Lounge Area",
            colorStyle = "Orange",
            letters = "RELAX",
            gridWords = listOf("RELAX", "REAL", "AXEL", "EAR", "AXE"),
            placements = listOf(
                WordPlacement("RELAX", 5, 0, true),
                WordPlacement("REAL", 5, 0, false),
                WordPlacement("EAR", 5, 1, false),
                WordPlacement("AXE", 4, 4, false),
                WordPlacement("AXEL", 5, 3, false)
            ),
            bonusWords = listOf("LEX")
        ),
        LevelSpec(
            id = 42,
            themeName = "Bicycle Rack",
            colorStyle = "Blue",
            letters = "RIDER",
            gridWords = listOf("RIDER", "RIDE", "DIRE", "RED", "DIE"),
            placements = listOf(
                WordPlacement("RIDER", 5, 0, true),
                WordPlacement("RIDE", 5, 0, false),
                WordPlacement("RED", 5, 4, false),
                WordPlacement("DIE", 4, 1, false),
                WordPlacement("DIRE", 5, 2, false)
            ),
            bonusWords = listOf("RID")
        ),
        LevelSpec(
            id = 43,
            themeName = "Sky Bridge",
            colorStyle = "Red",
            letters = "VIEWS",
            gridWords = listOf("VIEWS", "VIEW", "WISE", "SEW", "VIE"),
            placements = listOf(
                WordPlacement("VIEWS", 5, 0, true),
                WordPlacement("VIEW", 5, 0, false),
                WordPlacement("VIE", 3, 2, false),
                WordPlacement("WISE", 5, 3, false),
                WordPlacement("SEW", 5, 4, false)
            ),
            bonusWords = listOf("IVE")
        ),
        LevelSpec(
            id = 44,
            themeName = "Greenhouse",
            colorStyle = "Orange",
            letters = "BLOOM",
            gridWords = listOf("BLOOM", "BOOM", "LOOM", "BOO", "LOB"),
            placements = listOf(
                WordPlacement("BLOOM", 5, 0, true),
                WordPlacement("BOO", 5, 0, false),
                WordPlacement("LOOM", 5, 1, false),
                WordPlacement("BOOM", 3, 3, false),
                WordPlacement("LOB", 4, 2, false)
            ),
            bonusWords = listOf("MOO")
        ),
        LevelSpec(
            id = 45,
            themeName = "Zen Garden",
            colorStyle = "Blue",
            letters = "PEACE",
            gridWords = listOf("PEACE", "CAPE", "PACE", "ACE", "PEA"),
            placements = listOf(
                WordPlacement("PEACE", 5, 0, true),
                WordPlacement("PEA", 5, 0, false),
                WordPlacement("ACE", 5, 2, false),
                WordPlacement("CAPE", 5, 3, false),
                WordPlacement("PACE", 2, 4, false)
            ),
            bonusWords = listOf("CAP")
        ),
        LevelSpec(
            id = 46,
            themeName = "Music Hall",
            colorStyle = "Red",
            letters = "SONGSU",
            gridWords = listOf("SONGS", "SONG", "SONS", "SUNG", "SUN"),
            placements = listOf(
                WordPlacement("SONGS", 5, 0, true),
                WordPlacement("SONG", 3, 2, false),
                WordPlacement("SUN", 5, 0, false),
                WordPlacement("SONS", 5, 4, false),
                WordPlacement("SUNG", 2, 3, false)
            ),
            bonusWords = listOf("SUNG")
        ),
        LevelSpec(
            id = 47,
            themeName = "Lecture Room",
            colorStyle = "Orange",
            letters = "TALKS",
            gridWords = listOf("TALKS", "TALK", "SALT", "TASK", "ALT"),
            placements = listOf(
                WordPlacement("TALKS", 5, 0, true),
                WordPlacement("TALK", 2, 3, false),
                WordPlacement("TASK", 5, 0, false),
                WordPlacement("SALT", 5, 4, false),
                WordPlacement("ALT", 4, 2, true)
            ),
            bonusWords = listOf("SAL")
        ),
        LevelSpec(
            id = 48,
            themeName = "Sports Field",
            colorStyle = "Blue",
            letters = "PLAYS",
            gridWords = listOf("PLAYS", "PLAY", "SLAP", "PALS", "LAY"),
            placements = listOf(
                WordPlacement("PLAYS", 5, 0, true),
                WordPlacement("PLAY", 5, 0, false),
                WordPlacement("LAY", 5, 1, false),
                WordPlacement("SLAP", 3, 2, false),
                WordPlacement("PALS", 2, 4, false)
            ),
            bonusWords = listOf("SAY")
        ),
        LevelSpec(
            id = 49,
            themeName = "Design Lab",
            colorStyle = "Red",
            letters = "STYLE",
            gridWords = listOf("STYLE", "TYLE", "LETS", "YES", "SLY"),
            placements = listOf(
                WordPlacement("STYLE", 5, 0, true),
                WordPlacement("TYLE", 5, 1, false),
                WordPlacement("SLY", 5, 0, false),
                WordPlacement("LETS", 5, 3, false),
                WordPlacement("YES", 5, 2, false)
            ),
            bonusWords = listOf("ELY")
        ),
        LevelSpec(
            id = 50,
            themeName = "Final Stage",
            colorStyle = "Red",
            letters = "WINNERE",
            gridWords = listOf("WINNER", "WINE", "WREN", "INNER", "NEW"),
            placements = listOf(
                WordPlacement("WINNER", 5, 0, true),
                WordPlacement("WINE", 5, 0, false),
                WordPlacement("WREN", 4, 5, false),
                WordPlacement("INNER", 5, 1, false),
                WordPlacement("NEW", 5, 2, false)
            ),
            bonusWords = listOf("WIN", "INN", "ERE")
        )
    )
}
