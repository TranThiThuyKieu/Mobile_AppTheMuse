package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirebaseUserService
import com.example.appthemuse.domain.repository.UserRepository
import com.example.appthemuse.ui.model.UserUi

class UserRepositoryImpl(
    private val firebaseUserService: FirebaseUserService
) : UserRepository {

    override suspend fun getUserName(uid: String): String {
        return firebaseUserService.fetchUserName(uid)
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseUserService.getCurrentUserEmail()
    }

    override fun getCurrentUserUid(): String? {
        return firebaseUserService.getCurrentUserUid()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseUserService.isUserLoggedIn()
    }

    override suspend fun logout() {
        firebaseUserService.logout()
    }

    override suspend fun getFullUserProfile(uid: String): UserUi? {

        val data = firebaseUserService.fetchFullUserProfile(uid)
            ?: return null

        return UserUi(
            id = uid,
            username = data["username"]?.toString()
                ?: data["tên_người_dùng"]?.toString()
                ?: "Người dùng",

            email = firebaseUserService.getCurrentUserEmail()
                ?: data["email"]?.toString()
                ?: "",

            role = data["role"]?.toString() ?: "user",

            fullName = data["fullName"]?.toString()
                ?: data["ho_va_ten"]?.toString()
                ?: "",

            phoneNumber = data["phoneNumber"]?.toString()
                ?: data["so_dien_thoai"]?.toString()
                ?: "",

            birthday = data["birthday"]?.toString()
                ?: data["ngay_sinh"]?.toString()
                ?: "",

            gender = data["gender"]?.toString()
                ?: data["gioi_tinh"]?.toString()
                ?: "",

            isBlocked = false,

            favoriteGenres = emptyList(),

            avatarUrl = data["avatar_url"]?.toString()
                ?: data["avatarUrl"]?.toString()
                ?: data["ảnh_đại_diện"]?.toString()
                ?: ""
        )
    }

    override suspend fun saveUserProfile(
        uid: String,
        userUi: UserUi
    ): Boolean {

        val updateData = mapOf(
            "username" to userUi.username,
            "name" to userUi.username,
            "fullName" to userUi.fullName,
            "phoneNumber" to userUi.phoneNumber,
            "birthday" to userUi.birthday,
            "gender" to userUi.gender,
            "avatar_url" to userUi.avatarUrl
        )

        return firebaseUserService.updateUserProfile(
            uid,
            updateData
        )
    }

    override suspend fun getUserStats(uid: String): Triple<Int, Int, Int> {
        val readCount = firebaseUserService.countReadBooks(uid)
        val favoriteCount = firebaseUserService.countFavoriteBooks(uid)
        // downloadedCount được truyền từ ngoài (Room DB), trả 0 ở đây
        return Triple(readCount, favoriteCount, 0)
    }
}