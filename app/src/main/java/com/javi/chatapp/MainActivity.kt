package com.javi.chatapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private lateinit var user : FirebaseUser
    private lateinit var chatRoomsListView : ListView
    private lateinit var chatRooms : ArrayList<ChatRoom>
    private val RC_SIGN_IN = 1
    private val chatRoomItems = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build())

        // Create and launch sign-in intent
        this.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                this.user = FirebaseAuth.getInstance().currentUser!!
                setContentView(R.layout.activity_main)
                addChatRooms()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private fun addChatRooms(){
        val dbProvider = DbProvider()
        val listener: ()-> Unit = {
            this.chatRooms = dbProvider.getChatRooms()
            for(i in 0 until chatRooms.size) {
                val chatRoom = chatRooms[i]
                chatRoomItems.add(chatRoom.name)
            }

            this.setChatRoomsListView()
        }
        dbProvider.loadChatRooms(listener)
    }

    private fun setChatRoomsListView(){
        chatRoomsListView = findViewById(R.id.chatRoomsListView)

        if (chatRoomsListView.adapter == null){
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatRoomItems)
            chatRoomsListView = findViewById(R.id.chatRoomsListView)
            chatRoomsListView.adapter = adapter
        }
        val adapter =  chatRoomsListView.adapter as ArrayAdapter<*>
        adapter.notifyDataSetChanged()

        chatRoomsListView.setOnItemClickListener{_, _, position, _ ->
            this.showRoom(position)
        }
    }

    private fun showRoom(position: Int ){
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("userId", this.user.uid)
        intent.putExtra("chatRoomId", chatRooms[position].id)
        intent.putExtra("userName", this.user.displayName)
        startActivity(intent)
    }
}
