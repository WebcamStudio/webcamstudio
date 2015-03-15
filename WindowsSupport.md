# Introduction #
Here are discussed some MS Windows specifics.

# Building WCS on Windows #
  * Download the proper **static** [ffmpeg archive](http://ffmpeg.zeranoe.com/builds) for your architecture
  * Extract the archive in the appropriate place depending on your Windows (for example in a x86 OS in the _**C:\Program Files\FFMPEG**_ folder)
  * Add the path to the bin subfolder in the PATH environment variable of the system (for example the path _**C:\Program Files\FFMPEG\bin**_). You can find more info on how to do this in [this article from Microsoft](http://support.microsoft.com/kb/310519) or in [this thread](http://www.itechtalk.com/thread3595.html#post6240).
  * Download and install the latest version of the [NetBeans IDE](https://netbeans.org/)
  * [Checkout the source code of WebcamStudio](https://code.google.com/p/webcamstudio/source/checkout) in a folder
  * Open the project in NetBeans
  * Build the project
  * The WebcamStudio application is now in the _**dist**_ folder of the project. You may run it from inside NetBeans, or just move the _dist_ folder somewhere and start the application using this command:
```
java.exe -jar WebcamStudio.jar
```


# Windows porting issues #
There are a couple of specifics for the Windows porting:
  1. Even if WebcamStudio is build in Java, the virtual webcam part is not working as it is in Linux. For now, there is no solution implemented in WebcamStudio to provide a virtual webcam for Windows. A possible solution is integrating the virtual webcam from the [ucanvcam project](http://code.google.com/p/ucanvcam/).
  1. The GUI is mostly working by installing the GStreamer port for Windows.
  1. Some DirectShow desktop capture filters that can be used are:
    * [UScreenCapture](http://www.umediaserver.net/umediaserver/download.html)
    * [VH Screen Capture Driver](http://www.splitmedialabs.com/download), though it uses watermarking
    * [A GPL'd filter](https://github.com/rdp/screen-capture-recorder-to-video-windows-free)