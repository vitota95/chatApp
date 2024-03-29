package com.javi.chatapp

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

class DbProvider(val context: Context) {
    private val MESSAGES_PATH = "messages"
    private val CHAT_ROOMS_PATH = "chatRooms"

    private var rooms = ArrayList<ChatRoom>()
    private var messages = ArrayList<Message>()
    private var dbInstance = FirebaseFirestore.getInstance()
    private val storageRef  = FirebaseStorage.getInstance().reference
    private val addMessagesLock = ReentrantLock()

    private lateinit var messagesRegistration : ListenerRegistration

    fun getChatRooms() : ArrayList<ChatRoom>{
        return this.rooms
    }

    fun getMessages() : ArrayList<Message>{
        // added lock to avoid image messages call of listener concurrency
        this.addMessagesLock.lock()
        val msgs = ArrayList<Message>(this.messages)
        this.messages.clear()
        this.addMessagesLock.unlock()

        return msgs
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
            Log.e(TAG, "Error getting documents: ", exception)
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
            message.image = storagePath

            val ref = storageRef.child(storagePath)

            val uploadTask = ref.putFile(uri!! )

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                Log.e(TAG, "Could not upload the image. ")
            }
        }

        messagesCollection
            .add(message)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }
    }

    fun startReceivingMessages(messageListener : () -> Unit, roomId: String, numberOfItems : Long){
        val messagesCollection = dbInstance.collection(MESSAGES_PATH)

        this.messagesRegistration = messagesCollection
            .whereEqualTo("chatRoomId", roomId)
            .orderBy("date")
            .limit(numberOfItems)
            .addSnapshotListener { snapshot, e ->
                val documentChanges = snapshot?.documentChanges
                documentChanges?.forEach {
                    val message = it.document.toObject(Message::class.java)
                    if (message.image != null){
                        val ref  = storageRef.child(message.image!!)
                        try {
                            val localFile = File.createTempFile("image", null, this.context.cacheDir)

                            ref.getFile(localFile).addOnSuccessListener {
                                message.image = localFile.absolutePath
                                this.messages.add(message)
                                messageListener()
                            }.addOnFailureListener {
                                Log.e(TAG, "Couldn't retrieve the image: ", it)
                            }
                        }catch (e : IOException){

                        }
                    }
                    this.messages.add(message)
                }

                messageListener()
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
                Log.e(TAG, "failed to write the subscription to the database", it)
            }
    }
}
