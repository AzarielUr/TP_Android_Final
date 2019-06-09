package models

data class Todo(val id: String?, val title: String, val comment: String?, val done: Boolean = false, val image: String? = null)