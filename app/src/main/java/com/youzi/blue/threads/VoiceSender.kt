package com.youzi.blue.threads

import android.media.MediaRecorder
import android.util.Log
import com.youzi.blue.media.AACEncoder
import com.youzi.blue.io.VoicePack
import com.youzi.blue.media.AudioRecord
import com.youzi.blue.server.SendServerThread

/**
 *
 * @param s 发送线程
 * @param ChannelMode [AudioFormat.CHANNEL_IN_MONO] 或 [AudioFormat.CHANNEL_IN_STEREO]
 * @param EncodeFormat [AudioFormat.ENCODING_PCM_16BIT]
 * @param ChannelCount [AacFormat.ChannleOutOne]
 * @param ByteRate [AacFormat.ByteRate256Kbs]
 * @param SampleRate [AacFormat.SampleRate44100]
 */
class VoiceSender(var socketServer: SendServerThread,
                  var ChannelMode: Int, var EncodeFormat: Int,
                  var ChannelCount: Int, var ByteRate: Int,
                  var SampleRate: Int
) : AACEncoder.OnEncodeDone, AudioRecord.OnDataInput {

    val TAG = VoiceSender::class.java.name
    private val aacEncoder: AACEncoder = AACEncoder(ChannelCount, ByteRate, SampleRate)
    private val  audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SampleRate, ChannelMode, EncodeFormat
    )

    override fun onEncodeData(bytes: ByteArray, offset: Int, len: Int, ts: Long) {
        val voicePack = VoicePack(ChannelMode, EncodeFormat, ChannelCount,
                ByteRate, SampleRate, ts, bytes)
        socketServer.putVoicePack(voicePack)
    }

    fun exit() {
        Log.d(TAG, "退出中")
        audioRecord.release()
    }

    override fun onClose() {
        Log.d(TAG, "退出完成")
    }

    override fun inputData(bytes: ByteArray, offset: Int, len: Int) {
        val bytes1 = ByteArray(len)
        System.arraycopy(bytes, offset, bytes1, 0, len)
        aacEncoder.encode(bytes1)
    }

    override fun release() {
        aacEncoder.release()
    }


    init {
        aacEncoder.setOnEncodeDone(this)
        aacEncoder.init()
        audioRecord.setOnDataInput(this)
        audioRecord.init()
        audioRecord.start()
    }
}