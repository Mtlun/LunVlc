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
package com.lun.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lun.vlc.VideoPlayActivity;

public class MainActivity extends AppCompatActivity {

    private Button mPlay;
    private String mPath = "";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        mPlay = (Button) this.findViewById(R.id.bt_play);
        if (null == mPath || mPath.isEmpty()) {
            mPath = "/mnt/sdcard/video.3gp";//记得将video.3gp视频文件放到手机根目录(文件在工程根目录下)
        }
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPlayActivity.class);
                intent.putExtra("path", mPath);
                intent.putExtra("HWDecoderstatus", true);//设置为硬解码(默认硬解码)
                startActivity(intent);
            }
        });
    }

}
