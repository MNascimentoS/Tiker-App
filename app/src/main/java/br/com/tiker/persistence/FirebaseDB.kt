package br.com.tiker.persistence

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseDB {
    companion object {
        fun addOrUpdateUser(uid: String, isPremium: Boolean = false) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("")
            myRef.child("users").child(uid).setValue(UserDB(uid, isPremium))
        }

        fun setUserIsPremium(isPremium: Boolean) {
            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("")
            myRef.child("users").child(auth.uid!!).setValue(UserDB(auth.uid!!, isPremium))
        }

        fun getUserIsPremium(callback: ((Boolean) -> Unit)) {
            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val ref = database.reference
            ref.child("users").child(auth.uid!!).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isPremium = dataSnapshot.child("premium").value as Boolean? ?: false
                    callback(isPremium)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(false)
                }
            })
        }
    }
}