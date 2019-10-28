package com.javi.chatapp

import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class DbProvider {
    private val MESSAGES_PATH = "messages"
    private val CHAT_ROOMS_PATH = "chatRooms"

    private var rooms = ArrayList<ChatRoom>()
    private var messages = ArrayList<Message>()
    private var dbInstance = FirebaseFirestore.getInstance()
    private val storageRef  = FirebaseStorage.getInstance().reference

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

    fun sendMessage(message : Message){
        val messagesCollection = this.dbInstance.collection(MESSAGES_PATH)
        if (message.image != null){
            val uri = message.image!!.toUri()
            val storagePath = "images/${uri.lastPathSegment}"

            // set uri to the storage path in cloud store
            if (!message.isSentMessage()){
                message.image = storagePath
            }

            storageRef.child("images/${uri.lastPathSegment}")

            val uploadTask = storageRef.putFile(uri!!)

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                Log.d(TAG, "Could not upload the image. ")
            }
        }

        messagesCollection
            .add(message)
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun startReceivingMessages(listener : () -> Unit, roomId: String, numberOfItems : Long){
        val messagesCollection = dbInstance.collection(MESSAGES_PATH)

        this.messagesRegistration = messagesCollection
            .whereEqualTo("chatRoomId", roomId)
            .orderBy("date")
            .limit(numberOfItems)
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

    fun registerUserToChatRoom(uid : String, chatRoomId : String){
        val uidRef = this.dbInstance.collection("users").document(uid)
        val data = hashMapOf(
            "registeredChatRooms" to arrayListOf(chatRoomId)
        )

        uidRef.set(data, SetOptions.merge())
            .addOnFailureListener{
                Log.d(TAG, "failed to write the subscription to the database", it)
            }
    }
}
