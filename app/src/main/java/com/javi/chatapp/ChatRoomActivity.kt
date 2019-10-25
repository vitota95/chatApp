package com.javi.chatapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class ChatRoomActivity : AppCompatActivity(){
    private lateinit var uid : String
    private lateinit var chatRoomId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = getIntent()
        this.uid = intent.getStringExtra("userId")!!
        this.chatRoomId = intent.getStringExtra("chatRoomId")!!
        setContentView(R.layout.chat_room_activity)
    }

    fun sendMessage(v : View){
        val editText = findViewById<EditText>(R.id.editText)
        val text = editText.text.toString()
        val message = Message(text, this.chatRoomId, this.uid)

        val dbProvider = DbProvider()
        dbProvider.sendMessage(message)

        editText.text.clear()
    }

    private fun updateListView(){

    }
}