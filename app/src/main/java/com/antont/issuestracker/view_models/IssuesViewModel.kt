package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.widget.Toast
import com.antont.issuestracker.activities.LoginActivity
import com.antont.issuestracker.models.Comment
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IssuesViewModel(application: Application) : AndroidViewModel(application) {

    val issueList: MutableLiveData<MutableList<Issue>> = MutableLiveData()

    fun startLoginActivity() {
        logoutFromAccount()

        val intent = Intent(getApplication<Application>().applicationContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().applicationContext.startActivity(intent)
    }

    private fun logoutFromAccount() {
        FirebaseAuth.getInstance().signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        val client = GoogleSignIn.getClient(getApplication<Application>().applicationContext, gso)
        client.signOut()
        client.revokeAccess()
    }

    fun getIssuesData() {
        val issues: MutableList<Issue> = mutableListOf()

        val ref = FirebaseDatabase.getInstance().reference.child("issues")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                Toast.makeText(getApplication<Application>().applicationContext, "$databaseError", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (t: DataSnapshot? in dataSnapshot.children) {
                        t?.let { it1 -> issues.add(getIssueFromDataSnapshot(it1)) }
                    }
                    getIssueOwner(0, issues, issues[0].ownerId)
                }
            }
        })
    }

    private fun getIssueFromDataSnapshot(dataSnapshot: DataSnapshot): Issue {
        val date = dataSnapshot.child("date")?.value.toString()
        val description = dataSnapshot.child("description")?.value.toString()
        val owner = dataSnapshot.child("owner")?.value.toString()
        val status = dataSnapshot.child("status")?.value as Boolean
        val comments = getCommentsFromDataSnapshot(dataSnapshot.child("comments"))
        return Issue(owner, status, description, date, comments, null)
    }

    private fun getCommentsFromDataSnapshot(data: DataSnapshot): MutableList<Comment> {
        val comments: MutableList<Comment> = mutableListOf()
        for (t: DataSnapshot? in data.children) {
            comments.add(t?.getValue(Comment::class.java)!!)
        }
        return comments
    }

    private fun getIssueOwner(issuePos: Int, issues: MutableList<Issue>, ownerId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(ownerId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {

                    issues[issuePos].owner = it.getValue(User::class.java)!!
                    if (issuePos < issues.size - 1) {
                        getIssueOwner(issuePos + 1, issues, issues[issuePos + 1].ownerId)
                    } else {
                        issueList.value = issues
                    }
                }
            }
        })
    }
}