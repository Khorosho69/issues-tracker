package com.antont.issuestracker.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Issue(val ownerId: String, val status: Boolean, val description: String, val date: String,
                 val comments: List<Comment>?, var owner: User?) {
//    @Exclude
//    var owner: User? = null
//        @Exclude
//        set(value) {
//            field = value
//        }

    constructor() : this("", false, "", "", null, null)
}