package com.antont.issuestracker.models

import com.google.firebase.database.Exclude

data class Issue(val id: String, val owner: String, val title: String, val description: String, val date: String, val status: Boolean,
                 val comments: List<Comment>?, @Exclude var ownerRef: User?){

    constructor() : this("", "", "", "", "", false, null, null)
}
