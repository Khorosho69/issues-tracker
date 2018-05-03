package com.antont.issuestracker.activities

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.antont.issuestracker.R
import com.antont.issuestracker.UserRepository
import com.antont.issuestracker.fragments.CreateIssueDialog
import com.antont.issuestracker.fragments.IssueListFragment.*
import com.antont.issuestracker.models.User
import com.antont.issuestracker.view_models.IssuesViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_issues.*
import kotlinx.android.synthetic.main.nav_header_issues.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, UserRepository.OnUserFetched {

    private val issuesViewModel: IssuesViewModel by lazy {
        ViewModelProviders.of(this).get(IssuesViewModel::class.java)
    }

    lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { showCreateNewIssueDialog() }

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        issuesViewModel.startIssueListFragment(supportFragmentManager, ListType.ALL_ISSUES)

        UserRepository().fetchCurrentUser(this)

        navigationView.menu.getItem(1).isChecked = true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showCreateNewIssueDialog() {
        val dialog = CreateIssueDialog()
        dialog.show(supportFragmentManager, CREATE_ISSUE_DIALOG_TAG)
    }

    private fun updateNavigationHeaderItems(user: User) {
        val navigationHeaderView = navigationView.getHeaderView(0)

        Picasso.get()
                .load(user.profilePictUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(navigationHeaderView.navigationHeaderProfilePicture)

        navigationHeaderView.navigationHeaderUserName.text = user.name
        navigationHeaderView.navigationHeaderEmail.text = user.email
    }

    fun changeActionButtonVisibility(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_add_issue -> {
                showCreateNewIssueDialog()
            }
            R.id.nav_all_issues -> {
                this.setTitle(R.string.nav_menu_all_issues)
                issuesViewModel.startIssueListFragment(supportFragmentManager, ListType.ALL_ISSUES)
            }
            R.id.nav_my_issues -> {
                this.setTitle(R.string.nav_menu_user_issues_list_title)
                issuesViewModel.startIssueListFragment(supportFragmentManager, ListType.MY_ISSUES)
            }
            R.id.nav_logout -> {
                issuesViewModel.startLoginActivity()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onUserFetchedCallback(user: User) {
        currentUser = user
        updateNavigationHeaderItems(user)
    }

    companion object {
        private const val CREATE_ISSUE_DIALOG_TAG = "create_issue"
    }
}