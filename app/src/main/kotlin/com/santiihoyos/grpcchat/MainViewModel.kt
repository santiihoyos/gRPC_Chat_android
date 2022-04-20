package com.santiihoyos.grpcchat

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santiihoyos.grpcchat.data.grpc.model.grpcchat.*
import io.grpc.android.AndroidChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Server ChatService host.
 */
const val grpcChatServiceHost = "vps-7fe29e2f.vps.ovh.net"

/**
 * Server ChatService port.
 */
const val grpcChatServicePort = 8888

class MainViewModel : ViewModel() {

    /**
     * User identity
     */
    val user: MutableLiveData<User> = MutableLiveData()

    /**
     * Observable list of messages from server
     */
    val messages: MutableLiveData<MutableList<Message>> = MutableLiveData(mutableListOf())

    /**
     * ChatClient to speak with server ChatService
     */
    private lateinit var chatClient: ChatGrpc.ChatStub

    init {

        // when new user is detected relaunch getHistory and listen again.
        user.observeForever {
            getMessagesHistory()
            listenChatMessages()
        }
    }

    /**
     * Initialize chat and starts to listening.
     *
     * @param alias - [String]
     * @param appContext - [Context] required by AndroidChannelBuilder.
     */
    fun initChat(alias: String, appContext: Context) {
        val managedChannels = AndroidChannelBuilder
            .forAddress(grpcChatServiceHost, grpcChatServicePort)
            .context(appContext)
            .usePlaintext()
            .build()
        chatClient = ChatGrpc.newStub(managedChannels)
        chatClient.hello(
            Hello.newBuilder()
                .setNickName(alias)
                .build(),
            object : StreamObserver<User> {
                override fun onNext(value: User?) {
                    if (value != null) {
                        user.postValue(value)
                    }
                }

                override fun onError(t: Throwable?) {
                    t?.printStackTrace()
                }

                override fun onCompleted() {}
            }
        )
    }

    /**
     * Sends message to main Room
     *
     * @param - [String] message for sending.
     */
    fun sendMessage(message: String) {
        if (user.value == null) return
        user.value?.let { user ->
            chatClient.write(
                WriteMessage.newBuilder()
                    .setMessage(message)
                    .setUserId(user.id)
                    .build(),
                object : StreamObserver<MessageResult> {

                    override fun onNext(value: MessageResult?) {
                        Log.i("[MainViewmodel]", "Send result: ${value?.ack == ACK.SENT}")
                    }

                    override fun onError(t: Throwable?) {
                        t?.printStackTrace()
                    }

                    override fun onCompleted() {}
                }
            )
        }
    }

    private fun getMessagesHistory() {
        chatClient.getHistory(
            user.value,
            object : StreamObserver<History> {
                override fun onNext(value: History?) {
                    if (value != null) {
                        messages.postValue(value.messagesList)
                    }
                }

                override fun onError(t: Throwable?) {
                    t?.printStackTrace()
                }

                override fun onCompleted() {}
            }
        )
    }

    private fun listenChatMessages() {
        chatClient.listen(
            user.value,
            object : StreamObserver<Message> {
                override fun onNext(value: Message?) {
                    if (value != null) {
                        print(value)
                        messages.postValue(
                            mutableListOf(*messages.value!!.toTypedArray(), value)
                        )
                    }
                }

                override fun onError(t: Throwable?) {
                    t?.printStackTrace()
                }

                override fun onCompleted() {
                    getMessagesHistory()
                    listenChatMessages()
                }
            }
        )
    }
}
