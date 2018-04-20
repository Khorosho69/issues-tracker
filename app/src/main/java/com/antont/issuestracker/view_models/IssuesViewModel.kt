package com.antont.issuestracker.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.app.FragmentManager
import android.util.Log
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.activities.LoginActivity
import com.antont.issuestracker.fragments.IssueDetailFragment
import com.antont.issuestracker.fragments.IssueListFragment
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
    private lateinit var queryReference: Query

    val issueLiveData: MutableLiveData<Issue> = MutableLiveData()
    val issueListLiveData: MutableLiveData<MutableList<Issue>> = MutableLiveData()

    private val childEventListener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError?) {
            databaseError?.let { onRequestCanceled(it) }
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
            dataSnapshot?.let {
                val issue = getIssueFromDataSnapshot(dataSnapshot)
                updateIssueById(issue)
                issueLiveData.value = null
            }
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
            dataSnapshot?.let {
                val issue = getIssueFromDataSnapshot(dataSnapshot)
                issueListLiveData.value?.let {
                    it.add(issue)
                    getIssueOwner(it.last())
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {}
    }

    fun getIssueListRequest(listType: IssueListFragment.ListType) {
        issueListLiveData.value = mutableListOf()
        if (isDeviceOnline()) {
            databaseReference = FirebaseDatabase.getInstance().reference.child("issues")
            queryReference = databaseReference.orderByChild("owner").equalTo(FirebaseAuth.getInstance().currentUser?.uid)

            removeValueListener()

            if (listType == IssueListFragment.ListType.MY_ISSUES) {
                queryReference.addChildEventListener(childEventListener)
            } else {
                databaseReference.addChildEventListener(childEventListener)
            }
        } else {
            issueLiveData.value = null
            showNoInternetToast()
        }
    }

    private fun updateIssueById(issue: Issue) {
        issueListLiveData.value?.find({ i -> i.id == issue.id })?.commentsCount = issue.commentsCount
    }

    fun getIssueFromDataSnapshot(issuesDataSnapshot: DataSnapshot): Issue {
        val id = issuesDataSnapshot.child("id")?.value.toString()
        val owner = issuesDataSnapshot.child("owner")?.value.toString()
        val title = issuesDataSnapshot.child("title")?.value.toString()
        val description = issuesDataSnapshot.child("description")?.value.toString()
        val date = issuesDataSnapshot.child("date")?.value.toString()
        val commentsCount = issuesDataSnapshot.child("comments").childrenCount

        return Issue(id, owner, title, description, date, commentsCount, null)
    }

    private fun getIssueOwner(issue: Issue) {
        val issueId = issue.owner
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(issueId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                databaseError?.let { onRequestCanceled(it) }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    issue.ownerRef = it.getValue(User::class.java)!!
                    issueLiveData.value = issue
                }
            }
        })
    }

    private fun showNoInternetToast() {
        val context = getApplication<Application>().applicationContext
        Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
    }

    private fun isDeviceOnline(): Boolean {
        val context = getApplication<Application>().applicationContext
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo?.let {
            return networkInfo.isConnectedOrConnecting
        } ?: return false
    }

    fun removeValueListener() {
        databaseReference.removeEventListener(childEventListener)
        queryReference.removeEventListener(childEventListener)
    }

    fun postIssue(issueTitle: String, issueDescription: String) {
        if (isDeviceOnline()) {
            val newIssueId = FirebaseDatabase.getInstance().reference.child("issues").push().key

            FirebaseAuth.getInstance().currentUser?.let {
                val ownerId = it.uid
                val date = Calendar.getInstance().time.toString()
                val issue = Issue(newIssueId, ownerId, issueTitle, issueDescription, date, 0, null)

                val ref = FirebaseDatabase.getInstance().reference.child("issues").child(newIssueId)
                ref.setValue(issue)
            }
        } else {
            showNoInternetToast()
        }
    }

    // Send a request to the firebase server to check if there is this user in the database
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
                        // If data is null, then need to create a new user in the firebase database
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
        Toast.makeText(getApplication<Application>().applicationContext, databaseError.details, Toast.LENGTH_SHORT).show()
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