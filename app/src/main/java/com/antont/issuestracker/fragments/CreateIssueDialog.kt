package com.antont.issuestracker.fragments

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.R.id.createIssueDescription
import com.antont.issuestracker.R.id.createIssueTitle
import com.antont.issuestracker.activities.MainActivity
import com.antont.issuestracker.models.Issue
import com.antont.issuestracker.models.User
import com.antont.issuestracker.view_models.IssuesViewModel

class CreateIssueDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        val inflater = activity?.layoutInflater

        val dialogView = inflater?.inflate(R.layout.create_issue_layout, null)


        builder.setView(dialogView)
                .setTitle(getString(R.string.post_issue_dialog_title))
                .setPositiveButton(dialogView?.context?.getText(android.R.string.ok), { _, _ -> onPostIssueButtonClick(dialogView) })

        return builder.create()
    }

    private fun onPostIssueButtonClick(view: View?) {
        view ?: error("View is null")
        val issueTitleEditText: EditText = view.findViewById(createIssueTitle)
        val issueDescriptionEditText: EditText = view.findViewById(createIssueDescription)

        val issueTitle = issueTitleEditText.text.toString()
        val issueDescription = issueDescriptionEditText.text.toString()

        val user = (activity as MainActivity).currentUser

        if (issueTitle.isNotEmpty() and issueDescription.isNotEmpty()) {
            activity?.let { ViewModelProviders.of(it).get(IssuesViewModel::class.java).postIssue(user, issueTitle, issueDescription) }
        } else {
            Toast.makeText(view.context, getString(R.string.new_isssue_dialog_error_message), Toast.LENGTH_SHORT).show()
        }
    }
}