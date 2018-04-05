package com.antont.issuestracker.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.antont.issuestracker.R
import com.antont.issuestracker.adapters.RecyclerViewAdapter
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_issues.*
import kotlinx.android.synthetic.main.app_bar_issues.*
import kotlinx.android.synthetic.main.content_issues.*
import kotlinx.android.synthetic.main.nav_header_issues.view.*

class IssuesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var issuesViewModel: IssuesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues)
        setSupportActionBar(toolbar)

        showProgress(true)

        issuesViewModel = ViewModelProviders.of(this).get(IssuesViewModel::class.java)

        issuesViewModel.issueList.observe(this, Observer { t -> t?.let { setupRecyclerView(it) } })

        updateNavigationHeaderItems()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        issuesViewModel.getIssuesData()
    }

    private fun setupRecyclerView(users: MutableList<Issue>) {
        showProgress(false)
        users_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adapter = RecyclerViewAdapter(users)
        users_recycler_view.adapter = adapter
    }

    private fun showProgress(isLoading: Boolean) {
        users_recycler_view.visibility = if (isLoading) View.GONE else View.VISIBLE
        issues_progress_bar.visibility = if (isLoading) View.VISIBLE else View.GONE
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

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.issues, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_add_issue -> {

            }
            R.id.nav_logout -> {
                issuesViewModel.startLoginActivity()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


}
