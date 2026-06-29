package com.clearconsent.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.clearconsent.app.data.local.db.dao.*
import com.clearconsent.app.data.local.db.entity.*

@Database(
    entities = [
        SessionEntity::class,
        RecordingEntity::class,
        TranscriptEntity::class,
        TranscriptSegmentEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun recordingDao(): RecordingDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        private const val DB_NAME = "clearconsent.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
