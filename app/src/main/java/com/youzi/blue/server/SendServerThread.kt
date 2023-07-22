package com.youzi.blue.server

import com.youzi.blue.io.DataPack
import com.youzi.blue.io.Writable
import com.youzi.blue.net.common.protocol.Message
import com.youzi.blue.service.BlueService
import com.youzi.blue.utils.LoggerFactory
import com.youzi.blue.utils.toByteArray
import io.netty.channel.Channel
import java.util.concurrent.LinkedBlockingQueue

abstract class SendServerThread(channel: Channel) : Thread() {
    private val log = LoggerFactory.getLogger()
    private var clientChannel: Channel? = channel

    companion object {
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

    fun setChannelIsNull() {
        clientChannel = null
    }

    override fun run() {
        //Broken pipe 15次就退出录屏
        var failedTime = 0

        while (!exit) {
            val video: Writable? = bufferListVideo.peek()
            if (video != null) {
                if (clientChannel == null) {
                    log.error("网络断开, 停止录屏!")
                    BlueService.instace.stopRecord()
                } else {
                    val tmp = buildVideoPack(video)
                    val message = Message(Message.TYPE.RELAY, tmp)

                    clientChannel?.writeAndFlush(message)?.addListener { f ->
                        run {
                            if (f.isSuccess) {
                                log.info("has send video pack to server")
                            } else {
                                //发送失败大于15次就退出
                                failedTime++
                                if (failedTime >= 15) {
                                    BlueService.instace.stopRecord()
                                }
                                log.error("send video failed, reason: {}", f.cause().cause?.message)
                            }
                        }
                    }
                }


                bufferListVideo.remove(video)
            }
            /*val voice: Writable? = bufferListVoice.peek()
            if (voice != null) {
                val tmp = buildVoicePack(voice)
                val message = Message(Message.TYPE.RELAY, tmp)

                channel.writeAndFlush(message).addListener { f ->
                    run {
                        if (f.isCancellable) {
                            log.info("has send voice pack to server")
                        } else {
                            val message1 = f.cause().cause?.message
                            log.error("send voice failed, reason:  " + message1)

                            //Broken pipe 大于10次就退出
                            if (message1!!.contains("Broken pipe")) {
                                failedTime++
                                if (failedTime >= 10) {
                                    WorkAccessibilityService.instace.stopRecord()
                                }
                            }
                        }
                    }
                }


                bufferListVoice.remove(voice)
            }*/
        }
    }

    fun putVoicePack(writable: Writable) {
        bufferListVoice.offer(writable)
    }

    fun putVideoPack(writable: Writable?) {
        bufferListVideo.offer(writable)
    }

    fun exit() {
        log.warn("退出中")
        exit = true
        interrupt()
    }

    abstract fun onError(t: Throwable)

}