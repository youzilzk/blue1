package com.youzi.blue.media;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.youzi.blue.utils.LoggerFactory;

public class AudioRecord extends Thread {
    private final LoggerFactory log = LoggerFactory.getLogger();

    protected android.media.AudioRecord mAudioRecord;
    private final int mEncodeFormat;
    private final int mChannelMode;
    private final int mSampleRate;
    private final int mAudioSource;

    private boolean mExit = false;
    private OnDataInput mOnDataInput;

    /**
     * @param audioSource    音频源
     *                       详细音频源类型请查看{@link MediaRecorder.AudioSource}
     * @param sampleRateInHz 采样频率 默认44100
     *                       {@link AacFormat#SampleRate44100} {@link AacFormat#SampleRate48000}
     * @param channelConfig  声道采集配置
     *                       See {@link AudioFormat#CHANNEL_IN_MONO} and{@link AudioFormat#CHANNEL_IN_STEREO}.
     *                       {@link AudioFormat#CHANNEL_IN_MONO} is guaranteed to work on all devices.
     * @param audioFormat    采集格式
     *                       See {@link AudioFormat#ENCODING_PCM_8BIT}, {@link AudioFormat#ENCODING_PCM_16BIT},
     *                       and {@link AudioFormat#ENCODING_PCM_FLOAT}.
     */
    public AudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        super();
        this.mAudioSource = audioSource;
        this.mSampleRate = sampleRateInHz;
        this.mChannelMode = channelConfig;
        this.mEncodeFormat = audioFormat;

    }

    public void init() {
        int minBufferSize = android.media.AudioRecord.getMinBufferSize(mSampleRate, mChannelMode,
                mEncodeFormat);
        mAudioRecord = new android.media.AudioRecord(MediaRecorder.AudioSource.MIC,
                mSampleRate, mChannelMode, mEncodeFormat, minBufferSize * 2);
    }

    public void setOnDataInput(OnDataInput onDataInput) {
        this.mOnDataInput = onDataInput;
    }

    @Override
    public void run() {
        mAudioRecord.startRecording();
        //帧buffer 大小
        int mFrameSize = 2048;
        byte[] buffer = new byte[mFrameSize];
        int num;
        while (!mExit) {
            num = mAudioRecord.read(buffer, 0, mFrameSize);
            if (mOnDataInput != null) mOnDataInput.inputData(buffer, 0, num);
            log.info("buffer len " + ", num = " + num);
        }
        log.info("exit loop");

        distory();
        log.info("clean up");
    }


    private void distory() {
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        mOnDataInput.release();
        mOnDataInput = null;

    }

    public void release() {
        this.mExit = true;
    }

    public interface OnDataInput {
        void inputData(byte[] bytes, int offset, int len);

        void release();
    }
}
