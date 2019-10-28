package com.javi.chatapp

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList
import androidx.appcompat.app.AlertDialog



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
        this.showRegistrationToChatRoomDialog()
    }

    override fun onResume() {
        super.onResume()
        this.dbProvider.startReceivingMessages({ updateRoomMessages() }, this.chatRoomId, NUMBER_OF_MESSAGES)
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

    private fun showRegistrationToChatRoomDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.subscribe_notifications))
            .setPositiveButton(R.string.Yes, dialogClickListener())
            .setNegativeButton(R.string.No, dialogClickListener()).show()
    }

    private fun dialogClickListener() : DialogInterface.OnClickListener{
        return DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    this.dbProvider.registerUserToChatRoom(this.uid, this.chatRoomId)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }
    }
}