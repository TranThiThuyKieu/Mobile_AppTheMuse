package com.example.appthemuse.domain.repository

import com.example.appthemuse.ui.model.UserUi

interface UserRepository {

    suspend fun getUserName(uid: String): String

    fun getCurrentUserEmail(): String?

    fun getCurrentUserUid(): String?

    fun isUserLoggedIn(): Boolean

    suspend fun logout()

    suspend fun getFullUserProfile(uid: String): UserUi?

    suspend fun saveUserProfile(
        uid: String,
        userUi: UserUi
    ): Boolean

    // Đếm số liệu thống kê hồ sơ: đã đọc, yêu thích
    suspend fun getUserStats(uid: String): Triple<Int, Int, Int>
    //                                     readCount, favoriteCount, downloadedCount(truyền từ ngoài)
}