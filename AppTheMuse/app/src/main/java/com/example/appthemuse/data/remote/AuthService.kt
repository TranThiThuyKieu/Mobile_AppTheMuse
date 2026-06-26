package com.example.appthemuse.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun loginWithEmail(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user

        if (user != null && !user.isEmailVerified) {
            throw Exception("Email chưa được xác nhận")
        }

        return user
    }

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user

        user?.sendEmailVerification()?.await()
        return user
    }

    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(credential).await().user
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    // ✅ CHỈ GIỮ 1 HÀM CHECK
    suspend fun isEmailVerified(): Boolean {
        firebaseAuth.currentUser?.reload()?.await()
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    suspend fun sendEmailVerification() {
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}