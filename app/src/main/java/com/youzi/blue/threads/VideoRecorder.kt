package com.youzi.blue.threads

import android.media.projection.MediaProjection
import com.youzi.blue.media.Encoder.EncoderListener
import com.youzi.blue.media.MediaReader
import com.youzi.blue.io.VideoPack
import com.youzi.blue.io.SendServerThread
import com.youzi.blue.utils.LoggerFactory

/**
 *
 * @param st 发送线程
 * @param mp  MediaProjection
 * @param width 视频宽度 1080
 * @param height 视频高度 1920
 * @param videoBitrate 视频 比特率  16777216
 * @param videoFrameRate 视频 帧率 15
 */
class VideoRecorder(
    var sendServerThread: SendServerThread, mp: MediaProjection,
    var width: Int, var height: Int,
    var videoBitrate: Int, var videoFrameRate: Int
) : EncoderListener {
    private val log = LoggerFactory.getLogger()

    var mediaReader: MediaReader = MediaReader(
        width, height, videoBitrate,
        videoFrameRate, this, mp
    )

    override fun onH264(buffer: ByteArray, type: Int, ts: Long) {
        val datas = ByteArray(buffer.size)
        System.arraycopy(buffer, 0, datas, 0, buffer.size)
        val pack = VideoPack(
            datas, width, height, videoBitrate,
            videoFrameRate, type, ts
        )
        sendServerThread.putVideoPack(pack)
    }

    override fun onError(t: Throwable) {

    }

    fun exit() {
        log.info("正在退出")
        mediaReader.exit()
    }

    override fun onCloseH264() {
        log.info("退出完成")
    }


    init {
        mediaReader.init()
        mediaReader.start()
    }
}