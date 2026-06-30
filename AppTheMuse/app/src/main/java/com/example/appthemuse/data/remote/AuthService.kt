package com.example.appthemuse.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import android.util.Log

class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance().apply {
        setLanguageCode("vi")
    }

    // Đăng nhập bằng Email và Mật khẩu
    suspend fun loginWithEmail(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user

        if (user != null) {
            user.reload().await()
            if (!user.isEmailVerified) {
                throw Exception("Email chưa được xác nhận. Vui lòng kiểm tra hộp thư.")
            }
        }

        return user
    }

    // Đăng ký tài khoản mới bằng Email
    suspend fun registerWithEmail(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user

        try {
            user?.sendEmailVerification()?.await()
        } catch (e: Exception) {
            Log.e("AuthService", "Lỗi gửi email xác nhận ban đầu: ${e.message}")
        }
        return user
    }

    // Đăng nhập bằng tài khoản Google
    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(credential).await().user
    }

    // Lấy ID người dùng hiện tại
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    // Kiểm tra trạng thái xác thực email
    suspend fun isEmailVerified(): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            user?.reload()?.await()
            user?.isEmailVerified == true
        } catch (e: Exception) {
            false
        }
    }

    // Gửi lại email xác thực tài khoản
    suspend fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.reload().await()
            user.sendEmailVerification().await()
        } else {
            throw Exception("Không tìm thấy người dùng hiện tại để gửi email.")
        }
    }

    // Xóa tài khoản hiện tại khỏi Firebase Auth
    suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()?.await()
    }

    // Gửi email khôi phục mật khẩu khi quên
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    // Đăng xuất tài khoản
    fun signOut() {
        firebaseAuth.signOut()
    }
}