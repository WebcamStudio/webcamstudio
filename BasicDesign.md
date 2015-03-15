# Introduction #

This is a short description of the basic design of WebcamStudio.


# Details #

Overall, WebcamStudio is just a video and audio mixer (audio now supported in 0.60+ versions). It feeds on external applications such as [FFMpeg](http://www.ffmpeg.org/) ([libav](http://libav.org/)) or [GStreamer](http://gstreamer.freedesktop.org/) by launching a sub-process with the proper parameters.

In previous versions of WebcamStudio, libraries were used to interact with these components. But in time it became clear that it is quite complex to get good performance and stability. Also, the portability could be hazardous from one OS to another.

To solve this issue, WebcamStudio 0.60+ was completely redesigned from scratch to implement a new mechanism that would allow the use of any external applications, regardless of the system architecture (32bits vs 64 bits). Another benefit is that using external sub-processes ensures maximum efficiency when the computer is able to use multi-core, or when the application has been locally compiled for maximum performance.

The format currently supported by WebcamStudio for capturing and rendering is:
  * _Video:_ RGB24 pixel format of any size.
  * _Audio:_ Big Endian 16 bits, 2 channels (s16be).

This raw data must be made available through a local TCP/IP connection. This means that the external application must be able to output the raw data to a TCP/IP server connection (which WebcamStudio is handling) as fast or faster than a realtime playback.

For example, if WebcamStudio is running its internal mixer at 15 frames per second, each source must be able to feed WebcamStudio at 15 frames per second or faster. Otherwise, the mixer won't be able to keep up with a realtime rendering and lags will occur.

Here is some example external application usage:

```
ffmpeg -f video4linux2 -i /dev/video0 -s 320x240 -r 15 -f rawvideo -pix_fmt rgb24 tcp://127.0.0.1:2342
```
```
gst-launch v4l2src ! videoscale ! video/x-raw-rgb,width=320,height=240,framerate=15/1 ! ffmpegcolorspace ! video/x-raw-rgb,width=320,height=240,depth=24,bpp=24 ... ! tcpclientsink port=2342
```

Since some parameters need to be set when the process is launching (like the local port etc), WebcamStudio is using configuration files where the launching command uses tags that WCS fills with the proper parameter values.
For example:
```
ffmpeg ... -s @CWIDTHx@CHEIGHT -r @RATE ... tcp://127.0.0.1:@VPORT
```

Those tags can be found in [src/webcamstudio/externals/Tags.java](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/Tags.java)

The default configuration files can be found in:
  * [src/webcamstudio/externals/linux/sources](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/linux/sources)
  * [src/webcamstudio/externals/linux/outputs](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/linux/outputs)
  * [src/webcamstudio/externals/windows/sources](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/windows/sources)
  * [src/webcamstudio/externals/windows/outputs](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/windows/outputs)
  * [src/webcamstudio/externals/osx/sources](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/osx/sources)
  * [src/webcamstudio/externals/osx/outputs](https://code.google.com/p/webcamstudio/source/browse/trunk/src/webcamstudio/externals/osx/outputs)

In any _**"sources"**_ folder, there are 4 configuration files by default, representing _webcam_, _music_, _movie_ and _desktop_.

A user can override those files by having those files in a dedicated folder _**".webcamstudio"**_ in his home directory. For example, having _"webcam.properties"_ in _**"~/.webcamstudio"**_ will override the default launch command available in the official installed package.

If for some reasons a video/audio source is not available in WebcamStudio, a user can create a file with the extention _**".wss"**_ with the same structure as the structure of the source configuration files. This file can be added as another source or put in _**"~/.webcamstudio/sources"**_ to be automatically loaded on startup. This mechanism can then help a user to have a specific device integrated in WebcamStudio that can provide a video/audio source.