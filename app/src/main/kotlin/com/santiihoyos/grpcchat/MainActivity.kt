package com.santiihoyos.grpcchat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.ChatGrpc
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectToChatServer()
        setupButton()
    }

    private fun connectToChatServer() {
        GlobalScope.launch(Dispatchers.IO) {
            val managedChannels = AndroidChannelBuilder.forAddress("10.0.2.2", 24957)
                .context(this@MainActivity.applicationContext)
                .usePlaintext()
                .build()

            chatClient = ChatGrpc.newStub(managedChannels)
        }
    }

    private fun setupButton() {
        findViewById<Button>(R.id.button).setOnClickListener {
            chatClient.write(
                Message.newBuilder()
                    .setMessage(
                        findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
                    )
                    .setDateTime(Date().time)
                    .setUserId(249).build(),
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
        }
    }
}
