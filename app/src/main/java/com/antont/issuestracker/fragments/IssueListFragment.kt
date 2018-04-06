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
import com.antont.issuestracker.adapters.IssuesViewAdapter
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import kotlinx.android.synthetic.main.fragment_issues.*

class IssueListFragment : Fragment(), IssuesViewAdapter.OnItemSelectedCallback {
    private lateinit var issuesViewModel: IssuesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issues, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        issuesViewModel = ViewModelProviders.of(context as MainActivity).get(IssuesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showActionButton(true)

        issues_refresh_layout.setOnRefreshListener { issuesViewModel.getIssuesData() }

        issuesViewModel.issueList.value?.let {
            setupRecyclerView(it)
        } ?: kotlin.run {
            issuesViewModel.issueList.observe(this, Observer { t -> t?.let { setupRecyclerView(it) } })
            issuesViewModel.initialize()
        }
    }

    override fun onItemSelected(issuePosition: Int) {
        activity?.let {
            issuesViewModel.startIssueDetailFragment(it.supportFragmentManager, issuePosition)
        }

        showActionButton(false)
    }

    private fun setupRecyclerView(issues: MutableList<Issue>) {
        issues_refresh_layout.isRefreshing = false
        showProgress(false)
        users_recycler_view.layoutManager = LinearLayoutManager(context)
        val adapter = IssuesViewAdapter(issues, this)
        users_recycler_view.adapter = adapter
    }

    private fun showActionButton(visible: Boolean) {
        val parentActivity = activity as MainActivity
        parentActivity.showActionButton(visible)
    }

    private fun showProgress(isLoading: Boolean) {
        users_recycler_view.visibility = if (isLoading) View.GONE else View.VISIBLE
        issues_progress_bar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val FRAGMENT_TAG: String = "issues_fragment"
    }
}
