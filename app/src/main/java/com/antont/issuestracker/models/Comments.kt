package com.antont.issuestracker.models

data class Comments(val ownerId: String, val text: String, val date: String) {
    constructor() : this("", "", "")
}