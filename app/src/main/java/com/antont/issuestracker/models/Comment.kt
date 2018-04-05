package com.antont.issuestracker.models

data class Comment(val ownerId: String, val text: String, val date: String) {
    constructor() : this("", "", "")
}