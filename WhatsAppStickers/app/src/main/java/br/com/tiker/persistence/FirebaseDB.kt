package br.com.tiker.persistence

import com.google.firebase.database.FirebaseDatabase

class FirebaseDB {
    companion object {
        fun addOrUpdateUser(uid: String) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("")
            myRef.child("users").child(uid).setValue(UserDB(uid))
        }
    }
}