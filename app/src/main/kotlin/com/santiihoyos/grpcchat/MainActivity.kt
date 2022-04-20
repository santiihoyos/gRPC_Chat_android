package com.santiihoyos.grpcchat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecycler()
        setupButton()
        viewModel.initChat("AndroidUser", this.applicationContext)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecycler() {
        val adapterMessages = MessageItemAdapter(
            messages = mutableListOf(),
            userId = viewModel.user.value?.id ?: 0
        )
        val recycler = findViewById<RecyclerView>(R.id.messagesRecycler).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adapterMessages
        }
        viewModel.user.observe(this){ user ->
            adapterMessages.userId = user.id
            adapterMessages.notifyDataSetChanged()
        }
        viewModel.messages.observe(this) { messages ->
            adapterMessages.messages = messages ?: mutableListOf()
            adapterMessages.notifyDataSetChanged()
            recycler.scrollToPosition(messages.count() - 1)
        }
    }

    private fun setupButton() {
        findViewById<View>(R.id.button).setOnClickListener {
            val currentMessage = findViewById<EditText>(R.id.editTextTextPersonName).text
            viewModel.sendMessage(currentMessage.toString())
            currentMessage.clear()
        }
    }
}
