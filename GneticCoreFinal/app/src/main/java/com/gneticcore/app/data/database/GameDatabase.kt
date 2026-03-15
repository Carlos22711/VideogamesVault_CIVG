package com.gneticcore.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gneticcore.app.data.dao.CommentDao
import com.gneticcore.app.data.dao.GameDao
import com.gneticcore.app.data.dao.PendingChangeDao
import com.gneticcore.app.data.dao.UserDao
import com.gneticcore.app.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(
    entities = [User::class, Game::class, PendingChange::class, Comment::class, GameRating::class, CommentVote::class],
    version = 4,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gameDao(): GameDao
    abstract fun pendingChangeDao(): PendingChangeDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "gneticcore_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate admin user using a low-level SQL command to ensure it exists immediately
                        db.execSQL(
                            "INSERT OR IGNORE INTO users (username, displayName, password, role) " +
                            "VALUES ('admin', 'Admin', 'admin123', 'ADMIN')"
                        )
                    }
                })
                .build()
                .also { INSTANCE = it }
            }
    }
}
