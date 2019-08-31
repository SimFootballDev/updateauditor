package model

class UpdatePage(
    val title: String,
    val user: String,
    val lastPostIsRecent: Boolean,
    val lastPostIsFromOwner: Boolean
)