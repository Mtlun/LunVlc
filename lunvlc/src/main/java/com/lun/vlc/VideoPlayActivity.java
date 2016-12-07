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
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.videolan.libvlc.IVLCVout;

/**
 * Created by Ma.Tianlun on 2016/5/4.
 * Email: tianlun_ma@163.com
 */
public class VideoPlayActivity extends Activity implements CustomVLCVideoView.ONVLCPlayListener, IVLCVout.Callback {

    private CustomVLCVideoView mvlc;
    private RelativeLayout mBackView;
    private Button mback;

    private String mPath;
    private int mVideoWidth;
    private int mVideoHeight;
    private ProgressDialog mPd;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.videoplay_activity);
        mvlc = (CustomVLCVideoView) this.findViewById(R.id.vlc);
        mBackView = (RelativeLayout) this.findViewById(R.id.rl_back);
        mback = (Button) this.findViewById(R.id.bt_back);
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int flag = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        if (flag == 0) {
            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
        }

        mPath = getIntent().getStringExtra("path");
        mvlc.setIsShowSeekBar(true);
        mvlc.init(this, mPath, this, this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        this.mPath = savedInstanceState.getString("filename");
        mvlc.mPosition = savedInstanceState.getLong("position");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("filename", mPath);
        outState.putLong("position", mvlc.mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mBackView.setVisibility(View.GONE);
        } else {
            mBackView.setVisibility(View.VISIBLE);
        }
        mvlc.setSize(mVideoWidth, mVideoHeight);

    }

    @Override
    public void onPause() {
        super.onPause();
        mvlc.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mvlc.onResume(mPath);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mvlc.onDestroy();
        int flag = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        if (flag == 1) {
            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        }
    }

    @Override
    public void openingListener() {
        showProgressDialog("Loading...");
    }

    private void showProgressDialog(String msg) {
        mPd = new ProgressDialog(this);
        mPd.setMessage(msg);
        mPd.setCancelable(false);
        mPd.show();
    }

    @Override
    public void playingListener() {
        if (!isFinishing()) mPd.dismiss();
    }

    @Override
    public void pauseListener() {
    }

    @Override
    public void endListener() {
        if (!isFinishing()) {
            Toast.makeText(this, "play end~", Toast.LENGTH_SHORT).show();
//            finish();
        }
    }

    @Override
    public void stopListener() {

    }

    @Override
    public void encounteredErrorListener() {
        mPd.dismiss();
        Toast.makeText(this, "I 'm sorry, this video can't play", Toast.LENGTH_LONG).show();
//        finish();
    }

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0) {
            return;
        }
        mVideoWidth = width;
        mVideoHeight = height;
        mvlc.setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {
    }

}
