package com.antont.issuestracker.fragments

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.antont.issuestracker.R
import com.antont.issuestracker.models.Issue
import com.google.firebase.auth.FirebaseAuth
import java.util.*

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
                .setPositiveButton("Done", { _, _ -> listener.onIssueCreated(createIssue(dialogView)) })

        return builder.create()
    }

    private fun createIssue(view: View): Issue {
        val titleEditText: EditText = view.findViewById(R.id.create_issue_title)
        val descriptionEditText: EditText = view.findViewById(R.id.create_issue_description)

        val ownerId = FirebaseAuth.getInstance().currentUser?.uid
        val titleText = titleEditText.text.toString()
        val descriptionText = descriptionEditText.text.toString()
        val date = Calendar.getInstance().time.toString()

        return Issue(ownerId!!, false, titleText, descriptionText, date, null, null)
    }

    interface OnIssueCreatedCallback {
        fun onIssueCreated(newIssue: Issue)
    }
}