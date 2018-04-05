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

class RecyclerViewAdapter(private val issues: MutableList<Issue>) : RecyclerView.Adapter<RecyclerViewAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.issue_item_layout, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Picasso.get()
                .load(issues[position].owner?.profilePictUrl)
                .placeholder(R.drawable.profile_image_placeholder)
                .into(holder.imageView)
        holder.issueDescription.text = issues[position].description
        holder.issueOwner.text = "From: ${issues[position].owner?.name}"
        issues[position].comments?.let {
            holder.commentsCount.text = it.size.toString()
        } ?: run {
            holder.commentsCount.text = "0"
        }
    }

    override fun getItemCount(): Int {
        return issues.size
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(issue_item_image)
        val issueDescription: TextView = view.findViewById(issue_item_description)
        val commentsCount: TextView = view.findViewById(issue_item_message_count_view)
        val issueOwner: TextView = view.findViewById(issue_item_owner_name)
    }
}
