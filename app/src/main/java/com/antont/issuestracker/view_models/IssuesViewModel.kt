package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.support.v4.app.FragmentManager
import android.util.Log
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
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

class IssuesViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var databaseReference: DatabaseReference

    val issuesLivaData: MutableLiveData<MutableList<Issue>> = MutableLiveData()

    private val valueEventListener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError?) {
            databaseError?.let { error -> onRequestCanceled(error) }
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            dataSnapshot?.let {
                val issues: MutableList<Issue> = mutableListOf()

                for (issuesDataSnapshot: DataSnapshot? in dataSnapshot.children) {
                    issuesDataSnapshot?.let { issues.add(getIssueFromDataSnapshot(it)) }
                }
                if (issues.isNotEmpty()) {
                    getIssuesOwners(0, issues)
                } else {
                    issuesLivaData.value = issues
                    val context = getApplication<Application>().applicationContext
                    Toast.makeText(context, context.getString(R.string.no_issues_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getIssueListRequest(listType: IssueListFragment.ListType) {
        databaseReference = FirebaseDatabase.getInstance().reference.child("issues")

        if (listType == IssueListFragment.ListType.MY_ISSUES) {
            val query = databaseReference.orderByChild("owner").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
            query.addValueEventListener(valueEventListener)
        } else {
            databaseReference.addValueEventListener(valueEventListener)
        }
    }

    fun removeValueListener() {
        databaseReference.removeEventListener(valueEventListener)
    }

    fun postIssue(issueTitle: String, issueDescription: String) {
        val newIssueId = FirebaseDatabase.getInstance().reference.child("issues").push().key

        FirebaseAuth.getInstance().currentUser?.let {
            val ownerId = it.uid
            val date = Calendar.getInstance().time.toString()
            val issue = Issue(newIssueId, ownerId, issueTitle, issueDescription, date, false, null, null)

            val ref = FirebaseDatabase.getInstance().reference.child("issues").child(newIssueId)
            ref.setValue(issue)
        }
    }

    private fun getIssuesOwners(issuePos: Int, issues: MutableList<Issue>) {
        val issueId = issues[issuePos].owner
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(issueId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { onRequestCanceled(it) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    issues[issuePos].ownerRef = it.getValue(User::class.java)!!
                    if (issuePos < issues.size - 1) {
                        getIssuesOwners(issuePos + 1, issues)
                    } else {
                        issuesLivaData.value?.clear()
                        issuesLivaData.value = issues
                    }
                }
            }
        })
    }

    fun getIssueFromDataSnapshot(issuesDataSnapshot: DataSnapshot): Issue {
        val id = issuesDataSnapshot.child("id")?.value.toString()
        val owner = issuesDataSnapshot.child("owner")?.value.toString()
        val title = issuesDataSnapshot.child("title")?.value.toString()
        val description = issuesDataSnapshot.child("description")?.value.toString()
        val date = issuesDataSnapshot.child("date")?.value.toString()
        val status = issuesDataSnapshot.child("status")?.value as Boolean

        val comments = mutableListOf<Comment>()
        for (t: DataSnapshot? in issuesDataSnapshot.child("comments").children) {
            comments.add(t?.getValue(Comment::class.java)!!)
        }

        return Issue(id, owner, title, description, date, status, comments, null)
    }

    fun isUserExist() {
        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            val userId = firebaseUser.uid
            val ref = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError?) {
                    databaseError?.let { Log.d(this::javaClass.name, it.message) }
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let {
                        if (it.value == null) {
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
                }
            })
        }
    }

    private fun onRequestCanceled(databaseError: DatabaseError) {
        Toast.makeText(getApplication<Application>().applicationContext, "${databaseError.details}", Toast.LENGTH_SHORT).show()
    }

    private fun logoutFromAccount() {
        FirebaseAuth.getInstance().signOut()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        val client = GoogleSignIn.getClient(getApplication<Application>().applicationContext, googleSignInOptions)
        client.signOut()
        client.revokeAccess()
    }

    fun startIssueListFragment(supportFragmentManager: FragmentManager, listType: IssueListFragment.ListType) {
        var fragment = supportFragmentManager.findFragmentByTag(IssueListFragment.FRAGMENT_TAG)
        fragment?.let {
            (it as IssueListFragment).getIssues(listType)
        } ?: kotlin.run { fragment = IssueListFragment() }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.issues_frame, fragment, IssueListFragment.FRAGMENT_TAG)
                .commit()
    }

    fun startIssueDetailFragment(supportFragmentManager: FragmentManager, issueId: String) {
        var fragment = supportFragmentManager.findFragmentByTag(IssueDetailFragment.FRAGMENT_TAG)
        fragment ?: kotlin.run { fragment = IssueDetailFragment.newInstance(issueId) }
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.issues_frame, fragment, IssueDetailFragment.FRAGMENT_TAG)
                .addToBackStack(IssueDetailFragment.FRAGMENT_TAG)
                .commit()
    }

    fun startLoginActivity() {
        logoutFromAccount()

        val intent = Intent(getApplication<Application>().applicationContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        getApplication<Application>().applicationContext.startActivity(intent)
    }
}