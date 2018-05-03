package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableInt
import android.util.Log
import android.view.View
import android.widget.Toast
import com.antont.issuestracker.models.Comment
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.models.User
import com.google.android.gms.common.data.DataBufferObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_issue_detail.*
import java.util.*

class IssueDetailViewModel(application: Application) : AndroidViewModel(application) {

    var recyclerViewVisibility = ObservableInt(View.GONE)
    var progressBarVisibility = ObservableInt(View.VISIBLE)

    var issueLiveData: MutableLiveData<Issue> = MutableLiveData()
    val comments: MutableList<Comment> = mutableListOf()
    val commentLiveData: MutableLiveData<Comment> = MutableLiveData()

    private val valueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError?) {
            databaseError?.let { Log.d(IssueDetailViewModel::javaClass.name, "Request cancelled: ${databaseError.message}") }
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            dataSnapshot?.let { getIssueFromDataSnapshot(it) }
        }
    }

    private val childEventListener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError?) {
            databaseError?.let { onRequestCanceled(it) }
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            val comment = dataSnapshot?.getValue(Comment::class.java)!!
            comments.add(comment)
            getCommentOwner(comments.last())
        }

        override fun onChildRemoved(p0: DataSnapshot?) {}
    }

    fun fetchIssuesDetail(issueId: String) {
        showProgress(true)
        val databaseRef = FirebaseDatabase.getInstance().reference.child("issues").child(issueId)
        databaseRef.addListenerForSingleValueEvent(valueEventListener)
    }

    fun postComment(user: User, commentText: String) {
        issueLiveData.value?.let {
            val ref = FirebaseDatabase.getInstance().reference.child("issues").child(it.id).child("comments").push()

            val commentOwner = user.userId
            val date = Calendar.getInstance().time.toString()

            val comment = Comment(commentOwner, commentText, date, null)
            ref.setValue(comment)

        }
    }

    fun getIssueFromDataSnapshot(issuesDataSnapshot: DataSnapshot) {
        val issueId = issuesDataSnapshot.child("id")?.value.toString()
        val issueOwner = issuesDataSnapshot.child("owner")?.value.toString()
        val title = issuesDataSnapshot.child("title")?.value.toString()
        val description = issuesDataSnapshot.child("description")?.value.toString()
        val date = issuesDataSnapshot.child("date")?.value.toString()
        val commentsCount = issuesDataSnapshot.child("comments").childrenCount

        val issue = Issue(issueId, issueOwner, title, description, date, commentsCount, null)

        getIssueOwner(issue)
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
                    issueLiveData.value = issueRef

                    if (issueRef.commentsCount == 0L) {
                        showProgress(false)
                    }

                    val commentsRef = FirebaseDatabase.getInstance().reference.child("issues").child(issueRef.id).child("comments")
                    commentsRef.addChildEventListener(childEventListener)
                }
            }
        })
    }

    private fun getCommentOwner(comment: Comment) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(comment.owner)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { Log.d(this::javaClass.name, it.message) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let { issueData ->
                    comment.ownerRef = issueData.getValue(User::class.java)!!
                    commentLiveData.value = comment
                    showProgress(false)
                }
            }
        })
    }

    private fun showProgress(isLoading: Boolean) {
        recyclerViewVisibility.set(if (isLoading) View.GONE else View.VISIBLE)
        progressBarVisibility.set(if (isLoading) View.VISIBLE else View.GONE)
    }

    private fun onRequestCanceled(databaseError: DatabaseError) {
        Toast.makeText(getApplication<Application>().applicationContext, databaseError.details, Toast.LENGTH_SHORT).show()
    }
}