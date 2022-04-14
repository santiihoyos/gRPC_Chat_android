package com.santiihoyos.grpcchat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.ChatGrpc
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.HandShake
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.Message
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.MessageResult
import io.grpc.android.AndroidChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var chatClient: ChatGrpc.ChatStub
    private val userId by lazy {
        Date().time.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecycler()
        connectToChatServer()
    }

    private fun connectToChatServer() {
        GlobalScope.launch(Dispatchers.IO) {
            val managedChannels = AndroidChannelBuilder.forAddress("10.0.2.2", 8888)
                .context(this@MainActivity.applicationContext)
                .usePlaintext()
                .build()

            chatClient = ChatGrpc.newStub(managedChannels)
            runOnUiThread {
                listen()
                setupButton()
            }
        }
    }

    private fun setupRecycler() {
        val adapterMessages = MessagesAdapter(mutableListOf())
        findViewById<RecyclerView>(R.id.messagesRecycler).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adapterMessages
        }
    }

    private fun listen() {
        chatClient.listen(
            HandShake.newBuilder()
                .setUserId(userId)
                .setNick("AndroidUser-$userId")
                .build(),
            object : StreamObserver<Message> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(value: Message?) {
                    runOnUiThread {
                        findViewById<RecyclerView>(R.id.messagesRecycler).apply {
                            (adapter as MessagesAdapter).apply {
                                messages.add(value!!)
                            }.notifyDataSetChanged()
                        }
                    }
                }

                override fun onError(t: Throwable?) {
                    Log.e("[ERROR]", "${t?.cause}")
                }

                override fun onCompleted() {
                    Log.i("[INFO]", "strem closed")
                }
            }
        )
    }

    private fun setupButton() {
        findViewById<Button>(R.id.button).setOnClickListener {
            chatClient.write(
                Message.newBuilder()
                    .setMessage(
                        findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
                    )
                    .setDateTime(Date().time)
                    .setUserId(userId)
                    .build(),
                object : StreamObserver<MessageResult> {
                    override fun onNext(value: MessageResult?) {
                        println("Result: ${value?.wasOK}")
                    }

                    override fun onError(t: Throwable?) {
                        println("Result: $t")
                    }

                    override fun onCompleted() {
                        println("Result completed!")
                    }
                }
            )
            runOnUiThread {
                findViewById<EditText>(R.id.editTextTextPersonName).setText("")
            }
        }
    }
}
