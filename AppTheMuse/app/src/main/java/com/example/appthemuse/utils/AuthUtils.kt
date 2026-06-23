package com.example.appthemuse.utils

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthUtils {
    fun triggerGoogleSignIn(
        context: Context,
        autoSelect: Boolean = false,
        onTokenReceived: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("659976378491-9qj7cshcgejrqb40v37s7unp8qf2v89v.apps.googleusercontent.com")
            .setAutoSelectEnabled(autoSelect)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    onTokenReceived(credential.idToken)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
