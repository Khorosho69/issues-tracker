package com.antont.issuestracker.models

data class User(val userId: String, val name: String, val email: String, val profilePictUrl: String) {
    constructor() : this("", "", "", "")
}