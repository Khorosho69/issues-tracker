package com.antont.issuestracker.models

data class IssuesData (val users: List<User>?, val issues: List<Issue>?) {

    constructor() : this(null, null)
}