package com.antont.issuestracker.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class NotificationFirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshToken = FirebaseInstanceId.getInstance().token
        refreshToken?.let {
            Log.d(this.javaClass.simpleName, "New refresh token : $it")
            sendRegistrationToServer(it)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        FirebaseAuth.getInstance().currentUser?.let {
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(it.uid).child("token")
            ref.setValue(token)
        }
    }
}