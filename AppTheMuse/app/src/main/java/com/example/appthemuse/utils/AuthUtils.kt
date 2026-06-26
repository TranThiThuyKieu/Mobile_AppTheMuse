package com.example.appthemuse.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.appthemuse.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthUtils {
    private const val TAG = "AuthUtils"

    /**
     * Kích hoạt luồng đăng nhập Google bằng Credential Manager.
     */
    suspend fun triggerGoogleSignIn(
        context: Context,
        autoSelect: Boolean = false
    ): String? {
        val credentialManager = CredentialManager.create(context)
        
        // Lấy Web Client ID từ strings.xml
        val webClientId = context.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(autoSelect)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            
            // Sử dụng createFrom để lấy dữ liệu token chính xác từ bundle
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
            
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In Error: ${e.message}", e)
            
            val errorMessage = when (e.javaClass.simpleName) {
                "GetCredentialCanceledException" -> "Đã hủy đăng nhập"
                "NoCredentialException" -> "Không tìm thấy tài khoản Google hoặc lỗi cấu hình SHA-1/Package Name."
                "GetCredentialProviderConfigurationException" -> "Lỗi cấu hình Google (Hãy kiểm tra Web Client ID trên Firebase)"
                else -> "Lỗi đăng nhập Google: ${e.localizedMessage}"
            }
            
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            null
        }
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        val credentialManager = CredentialManager.create(context)
        CoroutineScope(Dispatchers.Main).launch {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            onComplete()
        }
    }
}
