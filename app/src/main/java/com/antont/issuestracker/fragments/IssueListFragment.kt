package com.antont.issuestracker.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.adapters.IssuesViewAdapter
import com.antont.issuestracker.databinding.FragmentIssueDetailBinding
import com.antont.issuestracker.databinding.FragmentIssueListBinding
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssuesViewModel
import kotlinx.android.synthetic.main.fragment_issue_list.*

class IssueListFragment : Fragment(), IssuesViewAdapter.OnItemSelectedCallback {

    private val issuesViewModel: IssuesViewModel by lazy {
        ViewModelProviders.of(activity!!).get(IssuesViewModel::class.java)
    }
    private var listType: ListType = ListType.ALL_ISSUES

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentIssueListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_list, container, false)
        binding.viewModel = issuesViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showActionButton(true)

        setupRecyclerView(issuesViewModel.issueList)
        fetchIssues(listType)

        issuesViewModel.issueLoaded.observe(this, Observer {
            it ?: return@Observer
            updateRecyclerView()
        })
    }

    override fun onItemSelected(issueId: String) {
        activity?.let {
            issuesViewModel.removeValueListener()
            issuesViewModel.startIssueDetailFragment(it.supportFragmentManager, issueId)
        }
        showActionButton(false)
    }

    fun fetchIssues(listType: ListType) {
        this.listType = listType
        issuesViewModel.fetchIssueList(listType)
    }

    private fun updateRecyclerView() {
        issuesRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setupRecyclerView(issues: MutableList<Issue>) {
        issuesRecyclerView.layoutManager = LinearLayoutManager(context)
        issuesRecyclerView.adapter = IssuesViewAdapter(issues, this)
    }

    // TODO DataBinding
    private fun showActionButton(visible: Boolean) {
        if (activity is MainActivity) {
            val parentActivity = activity as MainActivity
            parentActivity.changeActionButtonVisibility(visible)
        }
    }

    enum class ListType constructor(val value: Int) { ALL_ISSUES(0), MY_ISSUES(1) }

    companion object {
        const val FRAGMENT_TAG: String = "issues_fragment"
    }
}
