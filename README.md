#编译vlc原因
前段时间涉及到视频这块，使用vlc作为播放器，刚开始使用老早之前编译好的vlc，可是硬编码效果很不理想，甚至有些视频解码失败。
于是上网下载了官网的apk进行播放，不出所料可以正常播放视频，看来是官方做了优化。所以搜遍了整个互联网想直接下载个最近编译好的版本，结果都是那些老掉牙的版本，比之前编译的还老。于是自己下载vlc源码编译，最后编译通过。

**简单封装了下，有需要的直接拿去使用。后面也会长期不定时编译升级vlc版本。**

#LunVlc-library
##实现的功能
<pre>
* 简单封装
* 能支持MP4,FLV,AVI,TS,3GP,RMVB,WM,WMV等格式还有网络http,rtsp,rtmp,mms,m3u8.
* 支持软硬解码切换.
* 支持任意拖到进度条，支持是否显示进度条
</pre>
##使用方法
####引用库文件
	dependencies {
 	// jCenter
     compile 'com.github.Mtlun:lunvlc:1.0.2'
	}
####直接使用
我简单封装了一个activity可以直接使用（“path” 支持网络格式）(详细可以看demo)
如果想自定义实现可以参考 CustomVLCVideoView 和 VideoPlayActivity 的实现。

	添加权限
	<uses-permission android:name="android.permission.INTERNET"/>
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
	
	Intent intent = new Intent(MainActivity.this, VideoPlayActivity.class);
    intent.putExtra("path", mPath);
	intent.putExtra("HWDecoderstatus", true);//设置为硬解码(默认硬解码)
    startActivity(intent);

##说明
* jni文件在libvlc文件夹里面
* 目前支持的库 armeabi-v7a
* 后续会不定时编译升级vlc版本

##效果预览
![h](https://github.com/Mtlun/LunVlc/blob/master/screenshots/m.png?raw=true)

![h](https://github.com/Mtlun/LunVlc/blob/master/screenshots/v.png?raw=true)

![h](https://github.com/Mtlun/LunVlc/blob/master/screenshots/h.png?raw=true)

