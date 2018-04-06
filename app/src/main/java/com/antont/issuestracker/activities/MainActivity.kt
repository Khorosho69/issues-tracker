package com.antont.issuestracker.activities

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.antont.issuestracker.R
import com.antont.issuestracker.fragments.CreateIssueDialog
import com.antont.issuestracker.fragments.IssueListFragment
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_issues.*
import kotlinx.android.synthetic.main.nav_header_issues.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, CreateIssueDialog.OnIssueCreatedCallback {

    private lateinit var issuesViewModel: IssuesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        issuesViewModel = ViewModelProviders.of(this).get(IssuesViewModel::class.java)

        updateNavigationHeaderItems()

        fab.setOnClickListener { _ -> showCreateNewIssueDialog() }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            issuesViewModel.startIssueListFragment(supportFragmentManager)
        }
    }

    private fun showCreateNewIssueDialog() {
        val dialog = CreateIssueDialog()
        dialog.show(fragmentManager, CREATE_ISSUE_DIALOG_TAG)
    }

    override fun onIssueCreated(newIssue: Issue) {
        issuesViewModel.postNewIssue(newIssue)
    }

    private fun updateNavigationHeaderItems() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            val navigationHeaderView = navigationView.getHeaderView(0)

            Picasso.get()
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(navigationHeaderView.navigationHeaderProfilePicture)

            navigationHeaderView.navigation_header_user_name.text = user.displayName
            navigationHeaderView.navigation_header_email.text = user.email
        }
    }

    fun showActionButton(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.issues, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_add_issue -> {
                showCreateNewIssueDialog()
            }
            R.id.nav_user_issues -> {
                issuesViewModel.startIssueListFragment(supportFragmentManager)
            }
            R.id.nav_logout -> {
                issuesViewModel.startLoginActivity()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        private const val CREATE_ISSUE_DIALOG_TAG = "create_issue"
    }
}
