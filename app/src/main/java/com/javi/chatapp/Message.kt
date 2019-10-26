package com.javi.chatapp

import com.google.firebase.auth.FirebaseAuth
import java.util.Date

data class Message(var text : String = "", var chatRoomId : String = "", var uid : String = "",
                   var userName : String = "", var date: Date = Date(0))
{
    fun isSentMessage() : Boolean{
        return uid == FirebaseAuth.getInstance().currentUser?.uid
    }
}
