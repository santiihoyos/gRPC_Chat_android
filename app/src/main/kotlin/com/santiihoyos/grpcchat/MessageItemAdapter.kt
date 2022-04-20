package com.santiihoyos.grpcchat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.Message

class MessageItemAdapter(
    var messages: MutableList<Message>,
    var userId: Int,
) : RecyclerView.Adapter<MessageItemAdapter.MessagesHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageItemAdapter.MessagesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessagesHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageItemAdapter.MessagesHolder,
        position: Int,
    ) = holder.bind(userId, messages[position])

    override fun getItemCount(): Int = messages.count()

    inner class MessagesHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val messageContainerLinear =
            view.findViewById<LinearLayout>(R.id.messageItemContainer)
        private val messageSenderTextView = view.findViewById<TextView>(R.id.messageItemSender)
        private val messageMessageTextView = view.findViewById<TextView>(R.id.messageItemMessage)

        fun bind(userId: Int, message: Message) {
            val layoutParams = messageContainerLinear.layoutParams as FrameLayout.LayoutParams
            messageContainerLinear.apply {
                layoutParams.gravity = if (userId == message.user.id) Gravity.END else Gravity.START
                this.layoutParams = layoutParams
            }
            messageSenderTextView.text = message.user.nickName
            messageMessageTextView.text = message.message
        }
    }
}
