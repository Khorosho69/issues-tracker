package com.antont.issuestracker.activities

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.antont.issuestracker.R
import com.antont.issuestracker.fragments.CreateIssueDialog
import com.antont.issuestracker.fragments.IssueListFragment
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

        issuesViewModel.isUserExist()

        updateNavigationHeaderItems()

        fab.setOnClickListener { _ -> showCreateNewIssueDialog() }

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            issuesViewModel.startIssueListFragment(supportFragmentManager, IssueListFragment.ListType.ALL_ISSUES)
        }
    }

    private fun showCreateNewIssueDialog() {
        val dialog = CreateIssueDialog()
        dialog.show(fragmentManager, CREATE_ISSUE_DIALOG_TAG)
    }

    override fun postIssue(issueTitle: String, issueDescription: String) {
        issuesViewModel.postIssue(issueTitle, issueDescription)
    }

    private fun updateNavigationHeaderItems() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            val navigationHeaderView = navigationView.getHeaderView(0)

            Picasso.get()
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(navigationHeaderView.navigationHeaderProfilePicture)

            navigationHeaderView.navigationHeaderUserName.text = user.displayName
            navigationHeaderView.navigationHeaderEmail.text = user.email
        }
    }

    fun showActionButton(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_add_issue -> {
                showCreateNewIssueDialog()
            }
            R.id.nav_all_issues -> {
                issuesViewModel.startIssueListFragment(supportFragmentManager, IssueListFragment.ListType.ALL_ISSUES)
            }
            R.id.nav_my_issues -> {
                issuesViewModel.startIssueListFragment(supportFragmentManager, IssueListFragment.ListType.MY_ISSUES)
            }
            R.id.nav_logout -> {
                issuesViewModel.startLoginActivity()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        private const val CREATE_ISSUE_DIALOG_TAG = "create_issue"
    }
}
