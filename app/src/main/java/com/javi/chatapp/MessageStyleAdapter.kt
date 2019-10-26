package com.javi.chatapp

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MessageStyleAdapter(val context: Context) : BaseAdapter() {
    private val messages = ArrayList<Message>()

    fun addAllMessages(m : ArrayList<Message>){
        this.messages.addAll(m)
        notifyDataSetChanged()
    }

    fun addMessage(m : Message){
        this.messages.add(m)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Any {
        return messages.get(position)
    }

    override fun getItemId(position: Int): Long {
       return position.toLong()
    }

    override fun getCount(): Int {
        return messages.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val _convertView: View
        val message = messages.get(position)
        val inflater= context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val holder = MessageViewHolder()

        // Apply styles to messages depending on their origin
        if (message.isSentMessage()){
            _convertView = inflater.inflate(R.layout.sent_messages, parent, false)
        }
        else {
            _convertView = inflater.inflate(R.layout.received_message, parent, false)
            holder.name = _convertView?.findViewById(R.id.user_name) as TextView
            holder.name!!.text = message.userName
        }

        holder.messageBody = _convertView.findViewById(R.id.message_body) as TextView
        holder.messageBody!!.text = message.text
        _convertView.tag = holder

        return _convertView
    }

    class MessageViewHolder() {
        var name: TextView? = null
        var messageBody: TextView? = null
    }
}