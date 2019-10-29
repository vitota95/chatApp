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
import android.content.Intent
import android.provider.MediaStore
import android.app.Activity
import android.net.Uri

class ChatRoomActivity : AppCompatActivity(){
    private val NUMBER_OF_MESSAGES = 50L
    private val GALLERY_REQUEST_CODE = 100

    private val dbProvider = DbProvider(this)
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
        val message = Message(text, null, this.chatRoomId, this.uid, this.userName, Date(System.currentTimeMillis()))

        this.dbProvider.sendMessage(message)

        editText.text.clear()
    }

    private fun sendImageMessage(uri: Uri){
        val message = Message(null, uri.toString(), this.chatRoomId, this.uid, this.userName, Date(System.currentTimeMillis()))
        this.dbProvider.sendMessage(message)
    }

    private fun updateRoomMessages(){
        val messages = dbProvider.getMessages()

        this.messageStyleAdapter.addAllMessages(messages)
        this.findViewById<ListView>(R.id.messages_view).smoothScrollToPosition(messageStyleAdapter.count)
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

    fun pickImageFromGallery(v : View) {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    //data.getData return the content URI for the selected Image
                    val imageUri = data!!.data

                    // Set the image to the ImageView
                    this.sendImageMessage(imageUri!!)
                }
            }
    }
}