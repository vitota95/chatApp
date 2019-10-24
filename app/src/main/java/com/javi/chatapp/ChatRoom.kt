package com.javi.chatapp

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ChatRoom(val id: String) {

    companion object {
        fun getRoomsFromFile(file: String, context: Context): ArrayList<ChatRoom>
        {
            val chatRooms = ArrayList<ChatRoom>()
            try {
                val jsonString = loadJSONFromAssets(file, context)
                val json = JSONObject(jsonString)
                val chatRoomsJson = json.getJSONArray("chatRooms")

                (0 until chatRoomsJson.length()).mapTo(chatRooms) {
                    ChatRoom(chatRoomsJson.getJSONObject(it).getString("id"))
                }
            }
            catch (e: JSONException){
                e.printStackTrace()
            }

            return chatRooms
        }

        /// TODO: Move it to a utilities class
        private fun loadJSONFromAssets(file: String, context: Context): String? {
            var json: String? = null
            try {
                val inputStream = context.assets.open(file)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                json = String(buffer, Charsets.UTF_8)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return json
        }
    }
}
