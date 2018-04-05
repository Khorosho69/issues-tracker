package com.antont.issuestracker.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.adapters.RecyclerViewAdapter
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import kotlinx.android.synthetic.main.fragment_issues.*

class IssueListFragment : Fragment() {
    private lateinit var issuesViewModel: IssuesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issues, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        issuesViewModel = ViewModelProviders.of(context as MainActivity).get(IssuesViewModel::class.java)

        issuesViewModel.issueList.observe(this, Observer { t -> t?.let { setupRecyclerView(it) } })

        issuesViewModel.getIssuesData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        issues_refresh_layout.setOnRefreshListener { issuesViewModel.getIssuesData() }
    }

    private fun setupRecyclerView(issues: MutableList<Issue>) {
        issues_refresh_layout.isRefreshing = false
        showProgress(false)
        users_recycler_view.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        val adapter = RecyclerViewAdapter(issues)
        users_recycler_view.adapter = adapter
    }

    private fun showProgress(isLoading: Boolean) {
        users_recycler_view.visibility = if (isLoading) View.GONE else View.VISIBLE
        issues_progress_bar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val FRAGMENT_TAG: String = "issues_fragment"
    }
}
