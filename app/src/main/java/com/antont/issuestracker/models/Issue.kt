package com.antont.issuestracker.models

import com.google.firebase.database.Exclude

data class Issue(val id: String, val owner: String, val title: String, val description: String, val date: String,
                 var commentsCount: Long, @Exclude var ownerRef: User?){

    constructor() : this("", "", "", "", "", 0, null)
}
