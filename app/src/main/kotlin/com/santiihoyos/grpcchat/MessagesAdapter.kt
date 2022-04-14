package com.santiihoyos.grpcchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.Message

class MessagesAdapter(
    var messages: MutableList<Message>
) : RecyclerView.Adapter<MessagesAdapter.MessagesHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessagesAdapter.MessagesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, null, true)
        return MessagesHolder(view)
    }

    override fun onBindViewHolder(holder: MessagesAdapter.MessagesHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.count()

    inner class MessagesHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val messageTextView = view.findViewById<TextView>(R.id.messageText)

        fun bind(message: Message) {
            messageTextView.text = message.message
        }
    }
}
