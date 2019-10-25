package com.javi.chatapp

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DbProvider {
    var rooms = ArrayList<ChatRoom>()

    fun getChatRooms() : ArrayList<ChatRoom>{
        return this.rooms
    }

    fun loadChatRooms(listener : () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val roomsDocument = db.collection("chatRooms")

        roomsDocument.get().addOnSuccessListener { result  ->

            for (document in result.documents) {
                val chatRoom = document.toObject(ChatRoom::class.java)

                if (chatRoom != null){
                    this.rooms.add(chatRoom)
                }
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
        }.addOnCompleteListener {
            listener()
        }
    }
}
