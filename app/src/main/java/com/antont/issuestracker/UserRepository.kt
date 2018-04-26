package com.antont.issuestracker

import com.antont.issuestracker.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    var user: User? = null

    fun fetchCurrentUser(callback: OnUserFetched) {
        user?.let {
            callback.onUserFetchedCallback(it)
        } ?: kotlin.run {
            val issueId = FirebaseAuth.getInstance().currentUser?.uid
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(issueId)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError?) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let {
                        user = it.getValue(User::class.java)
                        user?.let { it1 -> callback.onUserFetchedCallback(it1) }
                    }
                }
            })
        }
    }

    interface OnUserFetched {
        fun onUserFetchedCallback(user: User)
    }
}