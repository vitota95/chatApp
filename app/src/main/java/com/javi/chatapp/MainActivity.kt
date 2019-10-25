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


class MainActivity : AppCompatActivity() {
    val chatRoomItems = ArrayList<String>()
    private val RC_SIGN_IN = 1
    private lateinit var chatRoomsListView : ListView

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
                val user = FirebaseAuth.getInstance().currentUser
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
        var listener: ()-> Unit = {
            val chatRooms = dbProvider.getChatRooms()
            for(i in 0 until chatRooms.size) {
                val chatRoom = chatRooms[i]
                chatRoomItems.add(chatRoom.name)
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatRoomItems)
            chatRoomsListView = findViewById(R.id.chatRoomsListView)
            chatRoomsListView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        dbProvider.loadChatRooms(listener)
    }
}
