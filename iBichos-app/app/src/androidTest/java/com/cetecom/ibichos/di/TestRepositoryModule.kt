package com.cetecom.ibichos.di

import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.repository.EventRepository
import com.cetecom.ibichos.domain.repository.ImageRepository
import com.cetecom.ibichos.domain.repository.InsectRepository
import com.cetecom.ibichos.domain.repository.SessionRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import com.cetecom.ibichos.fake.FakeAuthRepository
import com.cetecom.ibichos.fake.FakeCaptureRepository
import com.cetecom.ibichos.fake.FakeInsectRepository
import com.cetecom.ibichos.fake.FakeUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Módulo de Hilt para tests que reemplaza el RepositoryModule real.
 * Proporciona fakes ligeros para evitar dependencias de Firebase/red en tests de UI.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = FakeAuthRepository()

    @Provides
    @Singleton
    fun provideCaptureRepository(): CaptureRepository = FakeCaptureRepository()

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = FakeUserRepository()

    @Provides
    @Singleton
    fun provideSessionRepository(): SessionRepository = object : SessionRepository {
        override suspend fun startSession(userId: String): String = "fake_session"
        override suspend fun endSession(sessionId: String) {}
    }

    @Provides
    @Singleton
    fun provideEventRepository(): EventRepository = object : EventRepository {
        override suspend fun logLevelUp(userId: String, previousLevel: String, newLevel: String, xpAtEvent: Long) {}
        override suspend fun logMedalUnlocked(userId: String, medalId: String, xpAtEvent: Long) {}
        override suspend fun logSpeciesDiscovered(userId: String, scientificName: String, insectName: String, category: String, xpAtEvent: Long) {}
        override suspend fun logUserRegistered(userId: String) {}
    }

    @Provides
    @Singleton
    fun provideImageRepository(): ImageRepository = object : ImageRepository {
        override suspend fun upload(imageBytes: ByteArray): String = "https://fake-image-url.com/photo.jpg"
    }

    @Provides
    @Singleton
    fun provideInsectRepository(): InsectRepository = FakeInsectRepository()
}
