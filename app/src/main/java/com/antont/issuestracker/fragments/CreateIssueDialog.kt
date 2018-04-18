package com.antont.issuestracker.fragments

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.antont.issuestracker.R
import com.antont.issuestracker.R.id.createIssueDescription
import com.antont.issuestracker.R.id.createIssueTitle

class CreateIssueDialog : DialogFragment() {
    private lateinit var listener: OnIssueCreatedCallback

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = activity as OnIssueCreatedCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnIssueCreatedCallback")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater

        val dialogView = inflater.inflate(R.layout.create_issue_layout, null)


        builder.setView(dialogView)
                .setTitle(getString(R.string.post_issue_dialog_title))
                .setPositiveButton(dialogView.context.getText(android.R.string.ok), { _, _ -> onPostIssueButtonClick(dialogView) })

        return builder.create()
    }

    private fun onPostIssueButtonClick(view: View) {
        val issueTitleEditText: EditText = view.findViewById(createIssueTitle)
        val issueDescriptionEditText: EditText = view.findViewById(createIssueDescription)

        val issueTitle = issueTitleEditText.text.toString()
        val issueDescription = issueDescriptionEditText.text.toString()

        if (issueTitle.isNotEmpty() and issueDescription.isNotEmpty()) {
            listener.postIssue(issueTitle, issueDescription)
        } else {
            Toast.makeText(view.context, getString(R.string.new_isssue_dialog_error_message), Toast.LENGTH_SHORT).show()
        }
    }

    interface OnIssueCreatedCallback {
        fun postIssue(issueTitle: String, issueDescription: String)
    }
}