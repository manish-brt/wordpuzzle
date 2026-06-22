package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserStats::class,
        LevelGameplayState::class,
        LeaderboardEntry::class,
        DailyChallengeState::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wordscapes_crossword_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            val dao = getDatabase(context, scope).userProgressDao()
                            // Insert default user stats
                            dao.insertUserStats(UserStats())
                            
                            // Insert pre-populated competitors
                            val defaultCompetitors = listOf(
                                LeaderboardEntry("Lexicon_Lily", 2800, "#E91E63"),
                                LeaderboardEntry("Wiz_Wendy", 2150, "#9C27B0"),
                                LeaderboardEntry("Alice_Read", 1680, "#3F51B5"),
                                LeaderboardEntry("Puzzler_Pete", 1240, "#4CAF50"),
                                LeaderboardEntry("Quiz_Quincy", 850, "#FFC107"),
                                LeaderboardEntry("Wordy_Will", 520, "#FF5722"),
                                LeaderboardEntry("Brainy_Bob", 300, "#795548"),
                                LeaderboardEntry("Dictionary_Dave", 150, "#607D8B"),
                                LeaderboardEntry("You", 0, "#2196F3", isCurrentUser = true)
                            )
                            dao.insertLeaderboard(defaultCompetitors)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
