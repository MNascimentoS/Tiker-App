package br.com.tiker.persistence

data class UserDB(
    val uid: String,
    val premium: Boolean = false
)