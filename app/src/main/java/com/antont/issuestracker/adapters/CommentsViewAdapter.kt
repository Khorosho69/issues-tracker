package com.antont.issuestracker.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.antont.issuestracker.R
import com.antont.issuestracker.models.Comment
import com.antont.issuestracker.models.Issue
import com.squareup.picasso.Picasso

class CommentsViewAdapter(private val issue: Issue, private var comments: MutableList<Comment>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ViewType.HEADER.value -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.issue_header_layout, parent, false)
                return CommentsViewAdapter.HeaderViewHolder(v)
            }
            ViewType.COMMENT.value -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.comment_layout, parent, false)
                return CommentsViewAdapter.CommentsViewHolder(v)
            }
        }
        throw RuntimeException("There is no type that matches the type $viewType}")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.issueTitle.text = issue.title
                holder.issueDescription.text = issue.description
                holder.issueCreatedDate.text = "Created: ${issue.date}"
                issue.ownerRef?.let {
                    Picasso.get()
                            .load(it.profilePictUrl)
                            .placeholder(R.drawable.profile_image_placeholder)
                            .into(holder.issueOwnerProfilePict)
                    holder.issueOwnerName.text = it.name
                    holder.issueOwnerEmail.text = it.email
                }
            }
            is CommentsViewHolder -> {
                Picasso.get()
                        .load(comments[position - 1].ownerRef?.profilePictUrl)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(holder.imageView)
                holder.commentOwnerName.text = comments[position - 1].ownerRef?.name
                holder.commentText.text = comments[position - 1].text
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ViewType.HEADER.value
        } else ViewType.COMMENT.value
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val issueTitle: TextView = view.findViewById(R.id.issue_header_tittle)
        val issueDescription: TextView = view.findViewById(R.id.issue_header_description)
        val issueOwnerProfilePict: ImageView = view.findViewById(R.id.issue_header_owner_profile_pict)
        val issueOwnerName: TextView = view.findViewById(R.id.issue_header_user_name)
        val issueOwnerEmail: TextView = view.findViewById(R.id.issue_header_email)
        val issueCreatedDate: TextView = view.findViewById(R.id.issue_header_created_date)
    }

    class CommentsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.comment_profile_pict)
        val commentOwnerName: TextView = view.findViewById(R.id.comment_user_name)
        val commentText: TextView = view.findViewById(R.id.comment_text)
    }

    private enum class ViewType constructor(val value: Int) { HEADER(0), COMMENT(1) }
}