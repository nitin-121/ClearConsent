package com.clearconsent.app.di

import android.content.Context
import com.clearconsent.app.data.local.db.AppDatabase
import com.clearconsent.app.data.local.db.dao.*
import com.clearconsent.app.data.repository.SessionRepositoryImpl
import com.clearconsent.app.domain.repository.SessionRepository
import com.clearconsent.app.notification.NotificationHelper
import com.clearconsent.app.pipeline.SessionProcessingPipeline
import com.clearconsent.app.summarization.SummarizationEngine
import com.clearconsent.app.transcription.TranscriptionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.create(context)

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideRecordingDao(db: AppDatabase): RecordingDao = db.recordingDao()

    @Provides
    fun provideTranscriptDao(db: AppDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    fun provideSummaryDao(db: AppDatabase): SummaryDao = db.summaryDao()

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao,
        recordingDao: RecordingDao,
        transcriptDao: TranscriptDao,
        summaryDao: SummaryDao
    ): SessionRepository = SessionRepositoryImpl(sessionDao, recordingDao, transcriptDao, summaryDao)

    @Provides
    @Singleton
    fun provideTranscriptionEngine(@ApplicationContext context: Context): TranscriptionEngine =
        TranscriptionEngine(context)

    @Provides
    @Singleton
    fun provideSummarizationEngine(): SummarizationEngine = SummarizationEngine()

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper =
        NotificationHelper(context)

    @Provides
    @Singleton
    fun provideSessionProcessingPipeline(
        sessionRepository: SessionRepository,
        transcriptionEngine: TranscriptionEngine,
        summarizationEngine: SummarizationEngine,
        notificationHelper: NotificationHelper
    ): SessionProcessingPipeline = SessionProcessingPipeline(
        sessionRepository = sessionRepository,
        transcriptionEngine = transcriptionEngine,
        summarizationEngine = summarizationEngine,
        notificationHelper = notificationHelper
    )
}
