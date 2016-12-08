/*
 * Copyright Copyright (C) 2016 Ma Tianlun.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lun.vlc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Ma.Tianlun on 2016/5/3.
 * Email: tianlun_ma@163.com
 */
public class CustomVLCVideoView extends RelativeLayout {

    private Context mContext;
    private Activity mActivity;

    private SurfaceView mSurface;
    private RelativeLayout mSeeKManager;
    private MySeekBar mSeek;
    private CheckBox mStateBox;
    private TextView mTimeCurrent;
    private TextView mTimeTotal;

    private LibVLC mLibvlc;
    private SurfaceHolder mHolder;
    private int mVideoWidth;
    private int mVideoHeight;
    private String mFilePath;
    private boolean mIsShowSeekBar = true;
    private boolean mIsFirstBegin = true;
    private boolean mIsFinish = false;
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);
    private IVLCVout.Callback mCallback;
    private ONVLCPlayListener mONVLCPlayListener;
    private MediaPlayer mMediaPlayer = null;
    private boolean mHWDecoderEnabled = true;
    /**
     * Track point indicator
     */
    private int mPoint = -1;
    /**
     * Current playing position
     */
    public long mPosition;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mSeeKManager.setVisibility(View.GONE);
                    break;
                case 2:
                    if (mIsFirstBegin) {
                        int musicTime = (int) (mMediaPlayer.getLength() / 1000);
                        mTimeTotal.setText(String.format("%02d:%02d:%02d", musicTime / 3600, musicTime / 60, musicTime % 60));
                        mSeek.setMax((int) mMediaPlayer.getLength());
                        mSeek.setProgress(0);
                        mIsFirstBegin = false;
                    }
                    int musicTime = (int) (mMediaPlayer.getTime() / 1000);
                    mTimeCurrent.setText(String.format("%02d:%02d:%02d", musicTime / 3600, musicTime / 60, musicTime % 60));
                    mSeek.setProgress((int) mMediaPlayer.getTime());
                    break;
            }
        }
    };

    public CustomVLCVideoView(Context context) {
        super(context);
        this.mContext = context;
        initViews(mContext);
    }

    public CustomVLCVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initViews(mContext);
    }

    public CustomVLCVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initViews(mContext);
    }

    /**
     * Soft and hard decoding switch
     * @param status
     */
    public void setHWDecoderEnabled(boolean status){
        this.mHWDecoderEnabled = status;
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_custom_vlc_videoview, this);
        mSurface = (SurfaceView) view.findViewById(R.id.surface_view);
        mSeeKManager = (RelativeLayout) view.findViewById(R.id.rl_seekmanager);
        mSeek = (MySeekBar) view.findViewById(R.id.pb_seek);
        mStateBox = (CheckBox) view.findViewById(R.id.cb_state);
        mTimeCurrent = (TextView) view.findViewById(R.id.mediacontroller_time_current);
        mTimeTotal = (TextView) view.findViewById(R.id.mediacontroller_time_total);
    }

    public void init(Activity vActivity, String vFilePath, IVLCVout.Callback callback, ONVLCPlayListener oNVLCPlayListener) {
        this.mActivity = vActivity;
        this.mFilePath = vFilePath;
        Log.e("lun", "VideoPath:" + this.mFilePath);
        this.mCallback = callback;
        this.mONVLCPlayListener = oNVLCPlayListener;
        mHolder = mSurface.getHolder();
        if (!mIsShowSeekBar) {
            /** Hide progress bar **/
            mSeek.setVisibility(View.INVISIBLE);
            mTimeTotal.setVisibility(View.INVISIBLE);
            mTimeCurrent.setVisibility(View.INVISIBLE);
        }
        mSurface.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSeeKManager.getVisibility() == View.GONE) {
                    mSeeKManager.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(0, 3000);
                } else if (mSeeKManager.getVisibility() == View.VISIBLE) {
                    mSeeKManager.setVisibility(View.GONE);
                    mHandler.removeMessages(0);
                }
                return false;
            }
        });
        mSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
        mStateBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mIsFinish) {
                        createPlayer(mFilePath, mHWDecoderEnabled);
                        mIsFinish = false;
                    } else {
                        mMediaPlayer.play();
                    }
                } else {
                    mMediaPlayer.pause();
                }
            }
        });
        createPlayer(mFilePath, mHWDecoderEnabled);
    }

    public void createPlayer(String media, boolean needSetHWDecoderEnabled) {
        releasePlayer();
        try {
            // Create LibVLC
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mLibvlc = new LibVLC(mContext, options);
            mHolder.setKeepScreenOn(true);
            // Create media player
            mMediaPlayer = new MediaPlayer(mLibvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(mCallback);
            vout.attachViews();
            Media m = null;
            if (media.startsWith("rtsp://") || media.startsWith("http://"))
                m = new Media(mLibvlc, Uri.parse(media));
            else {
                m = new Media(mLibvlc, media);
            }
            if (needSetHWDecoderEnabled) {
                m.setHWDecoderEnabled(true, true);
            } else {
                m.setHWDecoderEnabled(false, false);
            }
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releasePlayer() {
        if (mLibvlc == null) {
            return;
        }
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(mCallback);
        vout.detachViews();
        mLibvlc.release();
        mLibvlc = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsFirstBegin = true;
        mPoint = -1;
    }

    /**
     * Set Surface
     */
    public void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (mHolder == null || mSurface == null)
            return;

        // get screen size
        int w = mActivity.getWindow().getDecorView().getWidth();
        int h = mActivity.getWindow().getDecorView().getHeight();
        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }
        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;
        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        mHolder.setFixedSize(mVideoWidth, mVideoHeight);
        // set display size
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    public void setIsShowSeekBar(boolean flag) {
        this.mIsShowSeekBar = flag;
    }

    private class OnSeekBarChangeListenerImp implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(0);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mMediaPlayer.setTime(seekBar.getProgress());
            mPoint = (int) (mMediaPlayer.getTime() / 1000);
            mTimeCurrent.setText(String.format("%02d:%02d:%02d", mPoint / 3600, mPoint / 60, mPoint % 60));
            mHandler.sendEmptyMessageDelayed(0, 3000);
        }
    }

    private class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<CustomVLCVideoView> mOwner;

        public MyPlayerListener(CustomVLCVideoView owner) {
            mOwner = new WeakReference<CustomVLCVideoView>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            CustomVLCVideoView player = mOwner.get();
            Log.e("lun", "VLC onEvent:" + event.type);
            if (mMediaPlayer.isPlaying()) {
                /** Set progress bar **/
                mHandler.sendEmptyMessage(2);
            }
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    mIsFinish = true;
                    mSeek.setProgress((int) mMediaPlayer.getLength());
                    player.releasePlayer();
                    mONVLCPlayListener.endListener();
                    break;
                case MediaPlayer.Event.Opening:
                    mONVLCPlayListener.openingListener();
                    if (mPosition > 0) {
                        mMediaPlayer.setTime(mPosition);
                        mSeek.setProgress((int) mMediaPlayer.getTime());
                        mPoint = (int) (mMediaPlayer.getTime() / 1000);
                        mPosition = 0;
                    }
                    break;
                case MediaPlayer.Event.EncounteredError:
                    releasePlayer();
                    mONVLCPlayListener.encounteredErrorListener();
                    break;
                case MediaPlayer.Event.Playing:
                    mONVLCPlayListener.playingListener();
                    mStateBox.setChecked(true);
                    break;
                case MediaPlayer.Event.Paused:
                    mONVLCPlayListener.pauseListener();
                    mStateBox.setChecked(false);
                    break;
                case MediaPlayer.Event.Stopped:
                    mONVLCPlayListener.stopListener();
                    mStateBox.setChecked(false);
                    break;
                default:
                    break;
            }
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public boolean isPlay() {
        return mMediaPlayer.isPlaying();
    }

    public void play() {
        mMediaPlayer.play();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public void onPause() {
        mPosition = mMediaPlayer.getTime();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void onResume(String pFilePath) {
        /** Resume playback */
        if (mPosition > 0) {
            try {
                createPlayer(pFilePath, mHWDecoderEnabled);
                mIsFinish = false;
            } catch (Exception e) {
                Log.e("lun", e.toString());
            }
        }
    }

    public void onDestroy() {
        releasePlayer();
    }

    public interface ONVLCPlayListener {
        public void openingListener();

        public void playingListener();

        public void pauseListener();

        public void endListener();

        public void stopListener();

        public void encounteredErrorListener();
    }

}
