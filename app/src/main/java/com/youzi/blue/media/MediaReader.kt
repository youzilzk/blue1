package com.youzi.blue.media

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.view.Surface
import com.youzi.blue.utils.LoggerFactory

/**
 * Created by Lesa on 2018/12/03.
 */
class MediaReader(//方向宽度
    protected var mWidth: Int, //方向高度
    protected var mHeight: Int, protected var videoBitrate: Int, protected var videoFrameRate: Int,
    encoderListener: EncoderListener?, private val mMediaProjection: MediaProjection
) : Encoder(mWidth, mHeight, videoBitrate, videoFrameRate, encoderListener) {
    private val log = LoggerFactory.getLogger()

    // parameters for the encoder
    private val mDpi = 1
    private var mSurface: Surface? = null
    private var mVirtualDisplay: VirtualDisplay? = null

    override fun init() {
        super.init()
        initVirtualDisplay()
    }


    private fun initVirtualDisplay() {
        mSurface = super.getmSurface()
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
            "blue-display",
            mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            mSurface, null, null
        )
        log.info("created virtual display: $mVirtualDisplay")
    }

    override fun onClose() {
        super.onClose()
        mVirtualDisplay?.release()
        mSurface?.release()
    }


}