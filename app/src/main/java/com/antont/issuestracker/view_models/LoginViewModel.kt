package com.antont.issuestracker.view_models

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.models.User
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == GOOGLE_SIGN_IN_CODE && resultCode == Activity.RESULT_OK) {
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
                    showLoginErrorToast()
                }
            }
        }
    }

    private fun showLoginErrorToast() {
        val context = getApplication<Application>().applicationContext
        Toast.makeText(context, R.string.signin_auth_error_message, Toast.LENGTH_SHORT).show()
    }

    fun isUserAuthorized(): Boolean {
        FirebaseAuth.getInstance().currentUser?.let {
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

    fun writeUserToFirebaseDatabase() {
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            val userId = firebaseUser.uid
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            val userName = firebaseUser.displayName
            val email = firebaseUser.email
            val profilePictUrl = firebaseUser.photoUrl.toString()
            val token = FirebaseInstanceId.getInstance().token

            token?.let {
                val user = User(userId, userName!!, email!!, profilePictUrl, token)
                ref.setValue(user)
            }
        }
    }

    fun starIssuesActivity() {
        val intent = Intent(getApplication<Application>().applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        getApplication<Application>().startActivity(intent)
    }

    companion object {
        const val GOOGLE_SIGN_IN_CODE: Int = 1001
    }
}