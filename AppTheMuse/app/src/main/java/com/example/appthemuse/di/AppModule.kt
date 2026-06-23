package com.example.appthemuse.di

import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirebaseUserService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.repository.AuthRepositoryImpl
import com.example.appthemuse.data.repository.UserRepositoryImpl
import com.example.appthemuse.domain.repository.AuthRepository
import com.example.appthemuse.domain.repository.UserRepository
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
    fun provideAuthService(): AuthService = AuthService()

    @Provides
    @Singleton
    fun provideFirestoreService(): FirestoreService = FirestoreService()

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: AuthService,
        firestoreService: FirestoreService // 👉 Đã thêm
    ): AuthRepository {
        return AuthRepositoryImpl(authService, firestoreService) // 👉 Đã sửa: Truyền đủ 2 service
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseUserService: FirebaseUserService
    ): UserRepository {
        return UserRepositoryImpl(firebaseUserService)
    }
}