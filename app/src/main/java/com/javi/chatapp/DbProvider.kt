package com.javi.chatapp

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DbProvider {
    private val MESSAGES_PATH = "messages"
    private val CHAT_ROOMS_PATH = "chatRooms"

    private var rooms = ArrayList<ChatRoom>()
    private var messages = ArrayList<Message>()
    private var dbInstance = FirebaseFirestore.getInstance()

    private lateinit var messagesRegistration : ListenerRegistration

    fun getChatRooms() : ArrayList<ChatRoom>{
        return this.rooms
    }

    fun getMessages() : ArrayList<Message>{
        val messages = ArrayList<Message>()
        messages.addAll(this.messages)
        this.messages.clear()

        return messages
    }

    fun loadChatRooms(listener : () -> Unit){
        val roomsCollection = this.dbInstance.collection(CHAT_ROOMS_PATH)

        roomsCollection.get().addOnSuccessListener {
            for (document in it.documents) {
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

    fun getMessagesByRoom(listener: () -> Unit, roomId: String, numOfItems : Long){
        val messagesCollection = this.dbInstance.collection(MESSAGES_PATH)

        messagesCollection
            .whereEqualTo("chatRoomId", roomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(numOfItems)
            .get()
            .addOnSuccessListener {
                for (document in it.documents) {
                    val message = document.toObject(Message::class.java)
                    this.messages.add(message!!)
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

    fun startReceivingMessages(listener : () -> Unit, roomId: String){
        val messagesCollection = dbInstance.collection(MESSAGES_PATH)

        this.messagesRegistration = messagesCollection
            .whereEqualTo("chatRoomId", roomId)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
            val documentChanges = snapshot?.documentChanges

            documentChanges?.forEach {
                val message = it.document.toObject(Message::class.java)

                this.messages.add(message)
            }

            listener()
        }
    }

    fun stopReceivingMessages() {
        this.messagesRegistration.remove()
    }
}
