package com.youzi.blue.server

import android.util.Log
import com.youzi.blue.io.DataPack

import com.youzi.blue.utils.toByteArray
import com.youzi.blue.io.Writable
import com.youzi.blue.net.common.protocol.Message
import io.netty.channel.Channel
import java.util.concurrent.LinkedBlockingQueue

abstract class ServerThread(private var channel: Channel) : Thread(TAG) {
    companion object {
        val TAG = ServerThread::class.java.name
        fun buildVideoPack(writable: Writable): ByteArray {
            val pack = DataPack(DataPack.TYPE_VIDEO, writable.toByteArray())
            return pack.toByteArray()
        }

        fun buildVoicePack(writable: Writable): ByteArray {
            val pack = DataPack(DataPack.TYPE_VOICE, writable.toByteArray())
            return pack.toByteArray()
        }
    }

    var exit = false
    var bufferListVideo: LinkedBlockingQueue<Writable> = LinkedBlockingQueue(100)
    var bufferListVoice: LinkedBlockingQueue<Writable> = LinkedBlockingQueue(100)


    override fun run() {

        while (!exit) {
            val video: Writable? = bufferListVideo.peek()
            if (video != null) {
                val tmp = buildVideoPack(video)
                val message = Message(Message.TYPE.RELAY, tmp)

                channel.writeAndFlush(message).addListener { f ->
                    run {
                        if (f.isCancellable) {
                            Log.d(TAG, "has send video pack to server")
                        } else {
                            Log.d(TAG, f.cause().message)
                        }
                    }
                }


                bufferListVideo.remove(video)
            }
            val voice: Writable? = bufferListVoice.peek()
            if (voice != null) {
                val tmp = buildVoicePack(voice)
                val message = Message(Message.TYPE.RELAY, tmp)

                channel.writeAndFlush(message).addListener { f ->
                    run {
                        if (f.isCancellable) {
                            Log.d(TAG, "has send voice pack to server")
                        } else {
                            Log.d(TAG, f.cause().message)
                        }
                    }
                }


                bufferListVoice.remove(voice)
            }
        }
    }

    fun putVoicePack(writable: Writable) {
        bufferListVoice.offer(writable)
    }

    fun putVideoPack(writable: Writable?) {
        bufferListVideo.offer(writable)
    }

    fun exit() {
        Log.d(TAG, "退出中")
        exit = true
        interrupt()
    }

    abstract fun onError(t: Throwable)

}