package com.antont.issuestracker.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.adapters.CommentsViewAdapter
import com.antont.issuestracker.databinding.FragmentIssueDetailBinding
import com.antont.issuestracker.models.Comment
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssueDetailViewModel
import kotlinx.android.synthetic.main.fragment_issue_detail.*

class IssueDetailFragment : Fragment() {

    private val issueDetailViewModel: IssueDetailViewModel by lazy {
        ViewModelProviders.of(this).get(IssueDetailViewModel::class.java)
    }
    private lateinit var selectedIssueId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedIssueId = it.getString(ARG_ISSUE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentIssueDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue_detail, container, false)
        binding.viewModel = issueDetailViewModel
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        issueDetailViewModel.issueLiveData.observe(this, Observer { it?.let { it1 -> setupRecyclerView(it1, issueDetailViewModel.comments) } })
        issueDetailViewModel.commentLiveData.observe(this, Observer {
            it?.let {
                notifyNewCommentAdded()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedIssueId.let {

            if (issueDetailViewModel.issueLiveData.value?.id == it) {
                setupRecyclerView(issueDetailViewModel.issueLiveData.value!!, issueDetailViewModel.comments)
            } else {
                issueDetailViewModel.fetchIssuesDetail(it)
            }

            postCommentButton.setOnClickListener {
                val commentText = commentTextEditText.text.toString()
                if (commentText.isNotEmpty()) {
                    val user = (activity as MainActivity).currentUser
                    issueDetailViewModel.postComment(user, commentTextEditText.text.toString())
                    commentTextEditText.text.clear()
                }
            }
        }
    }

    private fun setupRecyclerView(issue: Issue, comments: MutableList<Comment>) {
        val layoutManager = LinearLayoutManager(context)
        fragmentIssueDetailRecyclerView.layoutManager = layoutManager
        val adapter = CommentsViewAdapter(issue, comments)
        fragmentIssueDetailRecyclerView.adapter = adapter
    }

    private fun notifyNewCommentAdded() {
        fragmentIssueDetailRecyclerView.adapter.notifyDataSetChanged()
        fragmentIssueDetailRecyclerView.smoothScrollToPosition(fragmentIssueDetailRecyclerView.adapter.itemCount)
    }

    companion object {
        const val FRAGMENT_TAG: String = "issue_detail_fragment"
        const val ARG_ISSUE_ID: String = "ARG_ISSUE_ID"

        @JvmStatic
        fun newInstance(issueId: String) =
                IssueDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_ISSUE_ID, issueId)
                    }
                }
    }
}
