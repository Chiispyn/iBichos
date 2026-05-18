package com.cetecom.ibichos.di

import com.cetecom.ibichos.data.repository.AuthRepositoryImpl
import com.cetecom.ibichos.data.repository.CaptureRepositoryImpl
import com.cetecom.ibichos.data.repository.EventRepositoryImpl
import com.cetecom.ibichos.data.repository.ImageRepositoryImpl
import com.cetecom.ibichos.data.repository.InsectRepositoryImpl
import com.cetecom.ibichos.data.repository.SessionRepositoryImpl
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.repository.EventRepository
import com.cetecom.ibichos.domain.repository.ImageRepository
import com.cetecom.ibichos.domain.repository.InsectRepository
import com.cetecom.ibichos.domain.repository.SessionRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCaptureRepository(impl: CaptureRepositoryImpl): CaptureRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindInsectRepository(impl: InsectRepositoryImpl): InsectRepository
}
