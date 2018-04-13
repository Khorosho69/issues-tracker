package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.antont.issuestracker.models.Comment
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class IssueDetailViewModel(application: Application) : AndroidViewModel(application) {

    val issueLiveData: MutableLiveData<Issue>? = MutableLiveData()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    fun postComment(commentText: String) {
        issueLiveData?.value?.let {
            val ref = FirebaseDatabase.getInstance().reference.child("issues").child(it.id).child("comments").push()
            FirebaseAuth.getInstance().currentUser?.let {
                val commentOwner = it.uid
                val date = Calendar.getInstance().time.toString()

                val comment = Comment(commentOwner, commentText, date, null)
                ref.setValue(comment)
            }
        }
    }

    fun addValueEventListener(issueId: String) {
        valueEventListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { Log.d(IssueDetailViewModel::javaClass.name, "Request cancelled: ${databaseError.message}") }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let { getIssueFromDataSnapshot(it) }
            }
        }

        databaseReference = FirebaseDatabase.getInstance().reference.child("issues").child(issueId)
        databaseReference.addValueEventListener(valueEventListener)
    }

    fun getIssueFromDataSnapshot(issuesDataSnapshot: DataSnapshot) {
        val issueId = issuesDataSnapshot.child("id")?.value.toString()
        val issueOwner = issuesDataSnapshot.child("owner")?.value.toString()
        val title = issuesDataSnapshot.child("title")?.value.toString()
        val description = issuesDataSnapshot.child("description")?.value.toString()
        val date = issuesDataSnapshot.child("date")?.value.toString()
        val status = issuesDataSnapshot.child("status")?.value as Boolean

        val comments = mutableListOf<Comment>()
        val commentsDataSnapshot = issuesDataSnapshot.child("comments")
        val issue = Issue(issueId, issueOwner, title, description, date, status, comments, null)

        if (commentsDataSnapshot.childrenCount > 0) {
            for (t: DataSnapshot? in commentsDataSnapshot.children) {
                comments.add(t?.getValue(Comment::class.java)!!)
            }
            getCommentsOwners(0, issue, comments[0].owner)
        } else {
            getIssueOwner(issue)
        }
    }

    private fun getIssueOwner(issueRef: Issue) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(issueRef.owner)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { Log.d(this::javaClass.name, it.message) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let { issueData ->
                    issueRef.ownerRef = issueData.getValue(User::class.java)!!
                    issueLiveData?.value = issueRef
                }
            }
        })
    }

    private fun getCommentsOwners(commentPos: Int, issue: Issue, ownerId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(ownerId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { Log.d(this::javaClass.name, it.message) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let { issueData ->
                    issue.comments?.let {
                        issue.comments[commentPos].ownerRef = issueData.getValue(User::class.java)!!
                        if (commentPos < issue.comments.size - 1) {
                            getCommentsOwners(commentPos + 1, issue, issue.comments[commentPos + 1].owner)
                        } else {
                            getIssueOwner(issue)
                            issueLiveData?.value = issue
                        }
                    }
                }
            }
        })
    }

    fun removeValueListener() {
        databaseReference.removeEventListener(valueEventListener)
    }
}