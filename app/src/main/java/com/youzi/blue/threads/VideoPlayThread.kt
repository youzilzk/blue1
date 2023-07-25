package com.youzi.blue.threads

import android.util.Log
import android.view.Surface
import com.youzi.blue.media.Decoder
import com.youzi.blue.io.DataPackList
import com.youzi.blue.io.VideoPack

class VideoPlayThread(var surface: Surface, var inputdata: DataPackList) : Thread(TAG) {
    var exit = false
    var hasInitVideo = false
    lateinit var videodecoder: Decoder

    companion object {
        val TAG = VideoPlayThread::class.java.name
    }


    private fun initVideoDecoder(width: Int, height: Int, videoBitrate: Int, videoFrameRate: Int) {
        videodecoder = Decoder(width, height, videoFrameRate, surface)
        videodecoder.init()

    }

    override fun run() {
        while (!exit) {
            val videoPack: VideoPack? = inputdata.getVideoPack() as VideoPack?
            if (videoPack != null) {
                if (!hasInitVideo) {
                    Log.d(TAG, "video pack init $videoPack")
                    initVideoDecoder(
                        videoPack.width, videoPack.height,
                        videoPack.videoBitrate, videoPack.videoFrameRate
                    )
                    hasInitVideo = true
                    try {
                        sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                videodecoder.onFrame(
                    videoPack.frames,
                    0,
                    videoPack.frames.size,
                    videoPack.presentationTimeUs
                )
            }
        }
        dirtory()
    }

    private fun dirtory() {
        //有数据才会初始化解码器,这里防止空指针
        if (hasInitVideo) {
            videodecoder.release()
        }
        Log.i(TAG, "退出成功")
    }

    fun exit() {
        Log.i(TAG, "开始退出")
        exit = true
    }
}