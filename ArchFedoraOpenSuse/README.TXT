WebcamStudio 0.65

=======================
        UBUNTU
=======================
In Ubuntu you can install the latest WS build using the WebcamStudio PPA:
"https://launchpad.net/~webcamstudio/+archive/webcamstudio-dailybuilds"

If you want to use FFmpeg backend in Ubuntu 14.04 (ffmpeg is deprecated for this distro) open a terminal and type:

sudo add-apt-respository ppa:mc3man/trusty-media
sudo apt-get update
sudo apt-get install ffmpeg

=======================
   OPENSUSE/FEDORA
=======================

Update 01/05/2014 for NonUbuntu Users:
WS have to pass to gstreamer 1.x backend but seems that the 0.10 version is still best supported outside Ubuntu. For now I will let WS with the gstreamer 0.10 backend commands. Be sure you also install the gstreamer-0.10-ffmpeg package. For example gst-launch1.0 seems to not recognize "videoconvert" element or h264 decoding (gstreamer1.0-libav and all plugins are installed) ... maybe my Opensuse Installation is broken.
It would be very useful any feedback about gstreamer 1.0 backend outside Ubuntu :)

First of all add Pacman repository if not done yet.

Install gstreamer-0.10 with almost all plugins.

For gstreamer here there are the dependencies for WebcamStudio:

S | Name                                                 | Summary  | Type      
--+------------------------------------------------------+----------+-----------
i | gstreamer-0_10-plugin-gnomevfs                       | GStrea-> | package   
i | gstreamer-0_10-plugin-gnonlin                        | Non-li-> | package   
i | gstreamer-0_10-plugins-bad                           | GStrea-> | package   
i | gstreamer-0_10-plugins-bad-lang                      | Langua-> | package   
i | gstreamer-0_10-plugins-bad-orig-addon                | GStrea-> | package   
i | gstreamer-0_10-plugins-base                          | GStrea-> | package   
i | gstreamer-0_10-plugins-ffmpeg                        | GStrea-> | package   
i | gstreamer-0_10-plugins-fluendo_mp3                   | Fluend-> | package   
i | gstreamer-0_10-plugins-fluendo_mpegdemux             | Fluend-> | package   
i | gstreamer-0_10-plugins-fluendo_mpegmux               | Fluend-> | package   
i | gstreamer-0_10-plugins-good                          | GStrea-> | package   
i | gstreamer-0_10-plugins-good-extra                    | Comple-> | package   
i | gstreamer-0_10-plugins-ugly                          | GStrea-> | package   
i | gstreamer-0_10-plugins-ugly-orig-addon               | GStrea-> | package   
--+------------------------------------------------------+----------+-----------

Install "wmctrl" package also, to enable single desktop window capture.

Install "pactl" package for the Audiosource Stream-Panel.

Compile webcamstudio-module-109 (Shipped in this archive) and modprobe it.

Install libwebcamstudio:
For x64:
From folder WS_libx64 (Shipped in this archive) Copy libwebcamstudio.so in /usr/lib64 (or your user libs folder ...)
or
For x86:
From folder WS_libx86 (Shipped in this archive) Copy libwebcamstudio.so in /usr/lib

To use SkyCam you have to link "gksu" command to your existing gui-based-sudo installed Package.
For example if you have "gnomesu" installed, open a terminal and type:
$ sudo ln -s /usr/bin/gnomesu /usr/bin/gksu

Finally navigate where WebcamStudio.jar is and type from a terminal:
java -jar WebcamStudio.jar

Naturally for all streams and outputs you have to select "ffmpeg" backend if you didn't compile Avconv.
If you want you can compile libAV from sources. "http://libav.org/download.html"
For WS to work, please use this libAV ./configure settings:

$./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse

========================
      ARCH/MANJARO
========================

Update 01/05/2014 for NonUbuntu Users:
WS have to pass to gstreamer 1.x backend but seems that the 0.10 version is still best supported outside Ubuntu. For now I will let WS with the gstreamer 0.10 backend commands. Be sure you also install the gstreamer-0.10-ffmpeg package. For example gst-launch1.0 seems to not recognize "videoconvert" element or h264 decoding (gstreamer1.0-libav and all plugins are installed) ... maybe my Opensuse Installation is broken.
It would be very useful any feedback about gstreamer 1.0 backend outside Ubuntu :)

Compile gnonlin from source 0.10.17 ... "http://gstreamer.freedesktop.org/src/gnonlin/"
Once "sudo make install" is done, copy the gnonlin libs from "usr/local/lib/gstreamer" to "usr/lib/gstreamer" ... in Manjaro Linux ...

Install "wmctrl" package to enable single desktop window capture.
For SkyCam install "gksu" from AUR.

Install libwebcamstudio:
For x64:
From folder WS_libx64 (Shipped in the zip file) Copy libwebcamstudio.so in /usr/lib64 (or your user libs folder ...)
or
For x86:
From folder WS_libx86 (Shipped in the zip file) Copy libwebcamstudio.so in /usr/lib

Finally navigate where WebcamStudio.jar is and type from a terminal:
java -jar WebcamStudio.jar

Naturally for all streams and outputs you have to select "ffmpeg" backend if you didn't compile Avconv.
If you want you can compile libAV from sources. "http://libav.org/download.html"
For WS to work, please use this libAV ./configure settings:

$./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse

********************************************************
For any suggestions, comments or others please write to:
soylent.tv@gmail.com
********************************************************

Have a nice day.
karl.
