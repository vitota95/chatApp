package com.javi.chatapp

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DbProvider {
    private val MESSAGES_PATH = "messages"
    private val CHAT_ROOMS_PATH = "chatRooms"

    private var rooms = ArrayList<ChatRoom>()
    private var dbInstance = FirebaseFirestore.getInstance()

    private lateinit var messagesRegistration : ListenerRegistration

    fun getChatRooms() : ArrayList<ChatRoom>{
        return this.rooms
    }

    fun loadChatRooms(listener : () -> Unit){
        val roomsCollection = this.dbInstance.collection(CHAT_ROOMS_PATH)

        roomsCollection.get().addOnSuccessListener { result  ->

            for (document in result.documents) {
                val chatRoom = document.toObject(ChatRoom::class.java)
                if (chatRoom != null){
                    chatRoom.id = document.id
                    this.rooms.add(chatRoom)
                }
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
        }.addOnCompleteListener {
            listener()
        }
    }

    fun sendMessage(message : Message){
        val messagesCollection = this.dbInstance.collection(MESSAGES_PATH)

        messagesCollection
            .add(message)
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}
