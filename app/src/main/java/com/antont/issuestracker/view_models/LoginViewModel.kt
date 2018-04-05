package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Intent
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    fun onActivityResult(requestCode: Int, data: Intent) {
        if (requestCode == Companion.GOOGLE_SIGN_IN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            handleSignInResult(credential)
        }
    }

    private fun handleSignInResult(credential: AuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task: Task<AuthResult> ->
            run {
                if (task.isSuccessful) {
                    starIssuesActivity()
                } else {
                    Toast.makeText(getApplication<Application>().applicationContext, "Authorization error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun isUserAuthorized(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            starIssuesActivity()
            return true
        } ?: return false
    }


    fun getGoogleSingInIntent(): Intent {
        val requestIdToken = getApplication<Application>().resources.getString(R.string.default_web_client_id)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requestIdToken)
                .requestEmail()
                .build()

        val mGoogleApiClient = GoogleApiClient.Builder(getApplication<Application>().applicationContext)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        return Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
    }

    private fun starIssuesActivity() {
        val intent = Intent(getApplication<Application>().applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    companion object {
        const val GOOGLE_SIGN_IN_CODE: Int = 1001
    }
}