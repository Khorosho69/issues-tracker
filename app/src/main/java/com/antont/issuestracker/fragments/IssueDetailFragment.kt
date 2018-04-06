package com.antont.issuestracker.fragments

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.antont.issuestracker.R
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.adapters.CommentsViewAdapter
import com.antont.issuestracker.view_models.IssuesViewModel
import kotlinx.android.synthetic.main.fragment_issue_detail.*

class IssueDetailFragment : Fragment() {

    private lateinit var issuesViewModel: IssuesViewModel
    private var issuePosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            issuePosition = it.getInt(ARG_ISSUE_POSITION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_issue_detail, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        issuesViewModel = ViewModelProviders.of(context as MainActivity).get(IssuesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        issuePosition?.let {
            setupRecyclerView(it)
            val issueId = issuesViewModel.issueList.value?.get(it)?.id
            postCommentButton.setOnClickListener {
                issuesViewModel.postNewComment(issueId!!, comment_text_edit_text.text.toString())
                comment_text_edit_text.text.clear()
            }
        }

    }

    private fun setupRecyclerView(issueIndex: Int) {
        comments_recycler_view.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        issuesViewModel.issueList.value?.let {
            val adapter = CommentsViewAdapter(it[issueIndex])
            comments_recycler_view.adapter = adapter
        }
    }

    companion object {
        const val FRAGMENT_TAG: String = "issue_detail_fragment"
        const val ARG_ISSUE_POSITION: String = "issue_detail_fragment"

        @JvmStatic
        fun newInstance(issuePosition: Int) =
                IssueDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_ISSUE_POSITION, issuePosition)
                    }
                }
    }
}
