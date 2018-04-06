package com.antont.issuestracker.models

data class Comment(val owner: String, val text: String, val date: String, var ownerRef: User?)