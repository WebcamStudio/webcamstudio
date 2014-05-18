WebcamStudio 0.65

=======================
        UBUNTU
=======================
In Ubuntu you can install the latest WS build using the WebcamStudio PPA:
"https://launchpad.net/~webcamstudio/+archive/webcamstudio-dailybuilds"

If you want to use FFmpeg backend in Ubuntu 14.04 (ffmpeg is considered deprecated for this distro) open a terminal and type:

sudo add-apt-respository ppa:mc3man/trusty-media
sudo apt-get update
sudo apt-get install ffmpeg

=======================
   OPENSUSE/FEDORA
=======================

OpenSuse:

First of all add Packman repository if not done yet, and update all:
$ sudo zypper refresh
$ sudo zypper update

Fedora:

Add RPM Fusion Free / nonFree:
http://rpmfusion.org/Configuration
After adding the repository, open a terminal and with root privileges run:
# yum update

For Both:
--------
Step 1. Download the fonts from: http://font.ubuntu.com/resources/
( Download the Ubuntu Font Family › (1.5MB) )

Step 2. Unzip the fonts and rename them to your liking I renamed mine to "ubuntu".

Step 3. Open Terminal and type the following:
$ su -
# cd Downloads
# cp -R ubuntu /usr/share/fonts/

Step 4. Install gstreamer-0.10/gstreamer-1.0 with almost all plugins.

Gstreamer dependencies for WebcamStudio:

Fedora:

gstreamer.x86_64                          0.10.36-6.fc20  @koji-override-0/$releasever
gstreamer-ffmpeg.x86_64                   0.10.13-11.fc20 @rpmfusion-free-updates
gstreamer-plugins-bad.x86_64              0.10.23-5.fc20  @rpmfusion-free-updates
gstreamer-plugins-bad-free.x86_64         0.10.23-20.fc20 @updates              
gstreamer-plugins-bad-free-extras.x86_64  0.10.23-20.fc20 @updates              
gstreamer-plugins-bad-nonfree.x86_64      0.10.23-2.fc19  @rpmfusion-nonfree    
gstreamer-plugins-base.x86_64             0.10.36-6.fc20  @koji-override-0/$releasever
gstreamer-plugins-espeak.x86_64           0.4.0-2.fc19    @koji-override-0/$releasever
gstreamer-plugins-good.x86_64             0.10.31-10.fc20 @koji-override-0/$releasever
gstreamer-plugins-ugly.x86_64             0.10.19-14.fc20 @rpmfusion-free       
gstreamer-tools.x86_64                    0.10.36-6.fc20  @koji-override-0/$releasever

gstreamer1.x86_64                         1.2.3-1.fc20    @updates              
gstreamer1-libav.x86_64                   1.2.3-1.fc20    @rpmfusion-free-updates
gstreamer1-plugins-bad-free.x86_64        1.2.3-1.fc20    @updates              
gstreamer1-plugins-bad-free-extras.x86_64 1.2.3-1.fc20    @updates              
gstreamer1-plugins-bad-freeworld.x86_64   1.2.3-1.fc20    @rpmfusion-free-updates
gstreamer1-plugins-base.x86_64            1.2.3-1.fc20    @updates              
gstreamer1-plugins-base-tools.x86_64      1.2.3-1.fc20    @updates              
gstreamer1-plugins-good.x86_64            1.2.3-2.fc20    @updates              
gstreamer1-plugins-good-extras.x86_64     1.2.3-2.fc20    @updates              
gstreamer1-plugins-ugly.x86_64            1.2.3-1.fc20    @rpmfusion-free-updates
gnonlin.x86_64				  1.2.0-1.fc20    @updates

OpenSuse:

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

S | Name                                                 | Summary  | Type     
--+------------------------------------------------------+----------+-----------
i | gstreamer                                            | Stream-> | package
i | gstreamer-editing-services                           | GStrea-> | package  
i | gstreamer-lang                                       | Langua-> | package
i | gstreamer-libnice                                    | Intera-> | package
i | gstreamer-plugin-gnonlin                             | Non-li-> | package
i | gstreamer-plugin-gstclutter                          | GStrea-> | package
i | gstreamer-plugins-bad                                | GStrea-> | package
i | gstreamer-plugins-bad-orig-addon                     | GStrea-> | package
i | gstreamer-plugins-base                               | GStrea-> | package
i | gstreamer-plugins-farstream                          | GStrea-> | package
i | gstreamer-plugins-good                               | GStrea-> | package
i | gstreamer-plugins-good-extra                         | Comple-> | package
i | gstreamer-plugins-libav                              | GStrea-> | package
i | gstreamer-plugins-ugly                               | GStrea-> | package
i | gstreamer-plugins-ugly-orig-addon                    | GStrea-> | package
i | gstreamer-utils                                      | Stream-> | package
--+------------------------------------------------------+----------+-----------

Step 5. Install ffmpeg.

Fedora:
# yum install ffmpeg ffmpeg-libs gstreamer-ffmpeg

OpenSuse:
Install ffmpeg from packman repo.

Step 6. Install "wmctrl" package to enable single desktop window capture.

Step 7. Compile webcamstudio-module-109 (Shipped in this archive) and modprobe it.

Step 8. Install libwebcamstudio.
For x64:
From folder WS_libx64 (Shipped in this archive) Copy libwebcamstudio.so in /usr/lib64 (or your user libs folder ...)
For x86:
From folder WS_libx86 (Shipped in this archive) Copy libwebcamstudio.so in /usr/lib

Step 9. SkyCam.
To use SkyCam you have to link "gksu" command to your existing gui-sudo installed Package.
For example if you have "gnomesu" installed, open a terminal and type:
$ sudo ln -s /usr/bin/gnomesu /usr/bin/gksu
In Fedora there is "gksu-polkit":
# yum install gksu-polkit
# ln -s /usr/bin/gksu-polkit /usr/bin/gksu

Step 10. Navigate where WebcamStudio.jar is and type from a terminal:
$ java -jar WebcamStudio.jar

Naturally for all streams and outputs you can't select "avconv" backend (It will be hided) if you didn't compile Avconv.
If you want you can compile libAV from sources. "http://libav.org/download.html"
For WS to work, please use this libAV ./configure settings:

$./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse

TroubleShooting:
---------------
Opensuse:
First be sure that all gstreamer packages have "packman" as vendor.
To pass all packages to packman do:

$ sudo zypper dup --from packman

If the gstreamer backend doesn't work, maybe the codec cache is corrput.
I found the solution in this forum:
http://forums.opensuse.org/showthread.php/492716-openSUSE-13-1%28GNOME%29-codec-problem-mp4v-mpga-h264-mp4a/page3

And in particolar:
Remove ~/.cache/gstreamer-1.0/ if it exists.
Run "gst-inspect-1.0" (you may have to install "gstreamer-utils").
This should register all installed plugins.

========================
      ARCH/MANJARO
========================

This is not necessary now, because WS uses gnonlin 1.x, but maybe useful:

Compile gnonlin from source 0.10.17 ... "http://gstreamer.freedesktop.org/src/gnonlin/"
Once "sudo make install" is done, copy the gnonlin libs from "usr/local/lib/gstreamer" to "usr/lib/gstreamer" ... in 

Manjaro Linux ...

To have the correct visualization install Ubuntu fonts:

Step 1. Download the fonts from: http://font.ubuntu.com/resources/
( Download the Ubuntu Font Family › (1.5MB) )

Step 2. Unzip the fonts and rename them to your liking I renamed mine to "ubuntu".

Step 3. Open Terminal and type the following:
$ su -
# cd Downloads
# cp -R ubuntu /usr/share/fonts/

Install "gnonlin" from AUR.
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

Naturally for all streams and outputs you can't select "avconv" backend (It will be hided) if you didn't compile Avconv.
If you want you can compile libAV from sources. "http://libav.org/download.html"
For WS to work, please use this libAV ./configure settings:

$./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse

********************************************************
For any suggestions, comments or others please write to:
soylent.tv@gmail.com
********************************************************

Have a nice day.
karl.
