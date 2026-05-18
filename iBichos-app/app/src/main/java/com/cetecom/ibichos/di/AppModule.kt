package com.cetecom.ibichos.di

import com.cetecom.ibichos.data.remote.api.CloudinaryApi
import com.cetecom.ibichos.data.remote.api.KindwiseApi
import com.cetecom.ibichos.data.remote.CloudinaryModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideKindwiseApi(): KindwiseApi = KindwiseApi.create()

    @Provides
    @Singleton
    fun provideCloudinaryApi(): CloudinaryApi = CloudinaryModule.api
}
