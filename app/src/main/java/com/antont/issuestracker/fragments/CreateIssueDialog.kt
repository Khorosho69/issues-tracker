package com.antont.issuestracker.fragments

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.antont.issuestracker.R
import com.antont.issuestracker.R.id.create_issue_description
import com.antont.issuestracker.R.id.create_issue_title

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
                .setTitle("Create new issue")
                .setPositiveButton("Done", { _, _ ->
                    listener.onIssueCreated(getIssueTitle(dialogView, create_issue_title), getIssueTitle(dialogView, create_issue_description))
                })

        return builder.create()
    }

    private fun getIssueTitle(view: View, textId: Int): String {
        val titleEditText: EditText = view.findViewById(textId)
        return titleEditText.text.toString()
    }

    interface OnIssueCreatedCallback {
        fun onIssueCreated(issueTitle: String, issueDescription: String)
    }
}