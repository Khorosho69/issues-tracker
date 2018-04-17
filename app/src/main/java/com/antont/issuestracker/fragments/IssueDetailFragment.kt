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
import com.antont.issuestracker.adapters.CommentsViewAdapter
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.view_models.IssueDetailViewModel
import kotlinx.android.synthetic.main.fragment_issue_detail.*

class IssueDetailFragment : Fragment() {

    private lateinit var issueDetailViewModel: IssueDetailViewModel
    private lateinit var issuePosition: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            issuePosition = it.getString(ARG_ISSUE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue_detail, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        issueDetailViewModel = ViewModelProviders.of(context as MainActivity).get(IssueDetailViewModel::class.java)
        issueDetailViewModel.issueLiveData?.observe(this, Observer { it?.let { it1 -> setupRecyclerView(it1) } })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        issuePosition.let {
            issueDetailViewModel.addValueEventListener(it)
            postCommentButton.setOnClickListener {
                val commentText = commentTextEditText.text.toString()
                if (commentText.isNotEmpty()) {
                    issueDetailViewModel.postComment(commentTextEditText.text.toString())
                    commentsRecyclerView.scrollToPosition(commentsRecyclerView.adapter.itemCount - 1)
                    commentTextEditText.text.clear()
                }
            }
        }
    }

    private fun setupRecyclerView(issue: Issue) {
        val layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        commentsRecyclerView.layoutManager = layoutManager
        val adapter = CommentsViewAdapter(issue)
        commentsRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        issueDetailViewModel.removeValueListener()
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
