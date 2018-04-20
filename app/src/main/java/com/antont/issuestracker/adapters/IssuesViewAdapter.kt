package com.antont.issuestracker.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.antont.issuestracker.R
import com.antont.issuestracker.R.id.*
import com.antont.issuestracker.models.Issue
import com.squareup.picasso.Picasso

class IssuesViewAdapter(private val issues: MutableList<Issue>, private val listener: OnItemSelectedCallback) : RecyclerView.Adapter<IssuesViewAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.issue_item_layout, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener.onItemSelected(issues[position].id)
        }

        Picasso.get()
                .load(issues[position].ownerRef?.profilePictUrl)
                .placeholder(R.drawable.profile_image_placeholder)
                .into(holder.imageView)

        holder.issueTitle.text = issues[position].title
        holder.issueOwner.text = holder.itemView.resources.getString(R.string.issue_owner_prefiled_text, issues[position].ownerRef?.name)
        holder.commentsCount.text = issues[position].commentsCount.toString()
    }

    override fun getItemCount(): Int {
        return issues.size
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageView: ImageView = view.findViewById(issue_item_image)
        val issueTitle: TextView = view.findViewById(issue_item_title)
        val commentsCount: TextView = view.findViewById(issue_item_message_count_view)
        val issueOwner: TextView = view.findViewById(issue_item_owner_name)
    }

    interface OnItemSelectedCallback {
        fun onItemSelected(issueId: String)
    }
}
