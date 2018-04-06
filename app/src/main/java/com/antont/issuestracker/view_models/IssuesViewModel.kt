package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.support.v4.app.FragmentManager
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.activities.LoginActivity
import com.antont.issuestracker.fragments.IssueDetailFragment
import com.antont.issuestracker.fragments.IssueListFragment
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

    fun initialize() {
        isUserExist()
        getIssuesData()
    }

    fun getIssuesData() {
        val issues: MutableList<Issue> = mutableListOf()

        val ref = FirebaseDatabase.getInstance().reference.child("issues")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { onRequestCanceled(it) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (t: DataSnapshot? in dataSnapshot.children) {
                        t?.let { it1 -> issues.add(getIssueFromDataSnapshot(it1)) }
                    }
                    getIssuesOwners(0, issues, issues[0].owner)
                }
            }
        })
    }

    fun postNewIssue(issue: Issue) {
        val ref = FirebaseDatabase.getInstance().reference.child("issues").push()
        ref.setValue(issue)
    }

    private fun isUserExist() {
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            val userId = firebaseUser.uid
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError?) {
                    databaseError?.let { onRequestCanceled(it) }
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let {
                        if (it.value == null) {
                            val userName = firebaseUser.displayName
                            val email = firebaseUser.email
                            val profilePictUrl = firebaseUser.photoUrl.toString()
                            val user = User(userId, userName!!, email!!, profilePictUrl)

                            ref.setValue(user)
                        }
                    }
                }
            })
        }
    }

    private fun getIssueFromDataSnapshot(dataSnapshot: DataSnapshot): Issue {
        val date = dataSnapshot.child("date")?.value.toString()
        val title = dataSnapshot.child("title")?.value.toString()
        val description = dataSnapshot.child("description")?.value.toString()
        val owner = dataSnapshot.child("owner")?.value.toString()
        val status = dataSnapshot.child("status")?.value as Boolean
        val comments = getCommentsFromDataSnapshot(dataSnapshot.child("comments"))
        return Issue(owner, status, title, description, date, comments, null)
    }

    private fun getIssuesOwners(issuePos: Int, issues: MutableList<Issue>, ownerId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(ownerId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { onRequestCanceled(it) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {

                    issues[issuePos].ownerRef = it.getValue(User::class.java)!!
                    if (issuePos < issues.size - 1) {
                        getIssuesOwners(issuePos + 1, issues, issues[issuePos + 1].owner)
                    } else {
                        issueList.value = issues
                    }
                }
            }
        })
    }

    private fun getCommentsFromDataSnapshot(data: DataSnapshot): MutableList<Comment> {
        issueList.value?.clear()
        val comments: MutableList<Comment> = mutableListOf()
        for (t: DataSnapshot? in data.children) {
            t?.let {
                val owner: String = it.child("owner").value.toString()
                val text: String = it.child("text").value.toString()
                val date: String = it.child("date").value.toString()

                val ref = FirebaseDatabase.getInstance().reference.child("users").child(owner)

                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError?) {
                        databaseError?.let { onRequestCanceled(it) }
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        dataSnapshot?.let {
                            val commentOwner = dataSnapshot.getValue(User::class.java)!!
                            comments.add(Comment(owner, text, date, commentOwner))
                        }
                    }
                })
            }
        }
        return comments
    }

    private fun onRequestCanceled(databaseError: DatabaseError) {
        Toast.makeText(getApplication<Application>().applicationContext, "${databaseError.details}", Toast.LENGTH_SHORT).show()
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

    fun startIssueListFragment(supportFragmentManager: FragmentManager) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.issues_frame, IssueListFragment(), IssueListFragment.FRAGMENT_TAG)
                .commit()
    }

    fun startIssueDetailFragment(supportFragmentManager: FragmentManager, issuePos: Int) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.issues_frame, IssueDetailFragment.newInstance(issuePos), IssueDetailFragment.FRAGMENT_TAG)
                .addToBackStack(IssueListFragment.FRAGMENT_TAG)
                .commit()
    }

    fun startLoginActivity() {
        logoutFromAccount()

        val intent = Intent(getApplication<Application>().applicationContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().applicationContext.startActivity(intent)
    }
}