package com.antont.issuestracker.models

import com.google.firebase.database.Exclude

data class Comment(val owner: String, val text: String, val date: String, @Exclude var ownerRef: User?){

    constructor() : this("", "", "", null)
}