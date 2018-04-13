package com.antont.issuestracker.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.adapters.IssuesViewAdapter
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import kotlinx.android.synthetic.main.fragment_issues.*

class IssueListFragment : Fragment(), IssuesViewAdapter.OnItemSelectedCallback {
    private lateinit var issuesViewModel: IssuesViewModel
    var listType: ListType = ListType.ALL_ISSUES

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
        issuesRefreshLayout.setOnRefreshListener { getIssues(listType) }
        issuesViewModel.issuesLivaData.observe(this, Observer { mutableList ->
            mutableList?.let { setupRecyclerView(it) }
        })
        getIssues(listType)
    }

    override fun onItemSelected(issueId: String) {
        activity?.let {
            issuesViewModel.removeValueListener()
            issuesViewModel.startIssueDetailFragment(it.supportFragmentManager, issueId)
        }
        showActionButton(false)
    }

    fun getIssues(listType: ListType) {
        this.listType = listType
        issuesViewModel.getIssuesList(listType)
    }

    private fun setupRecyclerView(issues: MutableList<Issue>) {
        issuesRefreshLayout.isRefreshing = false
        showProgress(false)

        val layoutManager = LinearLayoutManager(context)

        issuesRecyclerView.layoutManager = layoutManager
        val adapter = IssuesViewAdapter(issues, this)
        issuesRecyclerView.adapter = adapter
    }

    private fun showActionButton(visible: Boolean) {
        val parentActivity = activity as MainActivity
        parentActivity.showActionButton(visible)
    }

    private fun showProgress(isLoading: Boolean) {
        issuesRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        issuesLoadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    enum class ListType constructor(val value: Int) { ALL_ISSUES(0), MY_ISSUES(1) }

    companion object {
        const val FRAGMENT_TAG: String = "issues_fragment"
    }
}
