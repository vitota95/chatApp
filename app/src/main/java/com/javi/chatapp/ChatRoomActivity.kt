package com.javi.chatapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomActivity : AppCompatActivity(){
    private val NUMBER_OF_MESSAGES = 50L

    private val dbProvider = DbProvider()
    private val roomMessages = ArrayList<String>()
    private val messageStyleAdapter = MessageStyleAdapter(this)

    private lateinit var messagesListView : ListView
    private lateinit var uid : String
    private lateinit var userName : String
    private lateinit var chatRoomId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_room_activity)

        val intent = getIntent()
        this.uid = intent.getStringExtra("userId")!!
        this.chatRoomId = intent.getStringExtra("chatRoomId")!!
        this.userName = intent.getStringExtra("userName")!!
        this.setMessagesListView()
        this.loadMessages()
        //this.dbProvider.startReceivingMessages({ updateRoomMessages() }, this.chatRoomId)
    }

    override fun onResume() {
        super.onResume()
        this.dbProvider.startReceivingMessages({ updateRoomMessages() }, this.chatRoomId)
    }

    override fun onPause() {
        super.onPause()
        this.roomMessages.clear()
        this.dbProvider.stopReceivingMessages()
    }

    fun sendMessage(v : View){
        val editText = findViewById<EditText>(R.id.editText)
        val text = editText.text.toString()
        val message = Message(text, this.chatRoomId, this.uid, this.userName, Date(System.currentTimeMillis()))

        this.dbProvider.sendMessage(message)

        editText.text.clear()
    }

    private fun loadMessages(){
        this.dbProvider.getMessagesByRoom({updateRoomMessages()}, this.chatRoomId, NUMBER_OF_MESSAGES)
    }

    private fun updateRoomMessages(){
        val messages = dbProvider.getMessages()
        val messagesText = messages.map { it.text }

        this.messageStyleAdapter.addAllMessages(messages)
        roomMessages.addAll(messagesText)
    }

    private fun setMessagesListView(){
        messagesListView = findViewById(R.id.messages_view)

        if (messagesListView.adapter == null){
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roomMessages)
            messagesListView.adapter = adapter
        }

        messagesListView.adapter = this.messageStyleAdapter
    }
}