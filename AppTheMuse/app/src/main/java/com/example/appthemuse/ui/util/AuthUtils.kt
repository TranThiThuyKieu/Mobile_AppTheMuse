package com.example.appthemuse.ui.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.appthemuse.R

object AuthUtils {

    suspend fun triggerGoogleSignIn(
        context: Context,
        autoSelect: Boolean = false,
        onTokenReceived: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        val webClientId = context.getString(R.string.default_web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(autoSelect)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is GoogleIdTokenCredential) {
                onTokenReceived(credential.idToken)
            }
        } catch (e: Exception) {
            Log.e("AuthUtils", "Lỗi Google Sign-In: ${e.localizedMessage}")
            // Bỏ qua lỗi nếu người dùng chủ động nhấn ra ngoài để hủy chọn tài khoản
            if (e.javaClass.simpleName != "GetCredentialCanceledException") {
                Toast.makeText(context, "Lỗi kết nối Google: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}