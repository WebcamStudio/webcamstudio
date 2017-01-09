# WebcamStudio 0.73

[![Maintainers Wanted](https://img.shields.io/badge/maintainers-wanted-red.svg)](https://github.com/pickhardt/maintainers-wanted)

We are searching for someone to hand over. For any questions, suggestions and comments, please write to: `soylent.tv@gmail.com`


WebcamStudio command line
--------------------------
```
webcamstudio [.studio file] -o [Output] -autoplay -remote
```
| Parameter        | Description |
| ---------        | ----------- |
| `[.studio file]` | The studio file that will be automatically loaded at start. |
| `-o [Output]`    | The Output button that will be enabled automatically at start. |
| `-autoplay`      | Will automatically start the first channel of the .studio file. |
| `-remote`        | Will automatically enable the Remote-Control at start. |

To specify the Output you will use the first name of the device (Case Sensitive):
```
[ WSVideoDevice - UDP - Ustream - IceCast - Audio - ...]
```
Naturally you must have the related button already added to WS.

Example command line:
```bash
$ java -jar WebcamStudio.jar /home/karl/Videos/Test.studio -o UDP -autoplay -remote
```

Install on Ubuntu
-----------------

In Ubuntu you can install the latest WS build using the WebcamStudio PPA:

https://launchpad.net/~webcamstudio/+archive/webcamstudio-dailybuilds

**If you want to manually install WebcamStudio from this archive, follow these steps:**

**1)** First make sure you have at least Java 7 installed (better the Oracle one) and that it is the default one.

**2)** Download the archive and extract it where you want.

**If you don't have the virtual webcam installed:**

Unpack `webcamstudio-module-112.tar.bz2`, navigate to the extracted folder, open a terminal and compile it:
```bash
$ make
$ sudo make install
```

Then `modprobe` the webcamstudio module:
```bash
$ sudo modprobe webcamstudio
```

**3)** Make sure that you have the `libwebcamstudio.so` library in your (64bit) `/usr/lib64` or (32bit) `/usr/lib` folder. If you don't have it:

**For x64:**  
From folder `WS_libx64` (Shipped in this archive) Copy `libwebcamstudio.so` to `/usr/lib64` (or your user libs folder ...)

**For x86:**  
From folder `WS_libx86` (Shipped in this archive) Copy `libwebcamstudio.so` to `/usr/lib`

**4)** WebcamStudio also uses JavaCV for some operations, and because this library differs from 32- to 64-bit versions, you have to make sure to use the correct ones **(very important)**:

In 32-bit machines, you have to remove the x86-64 JavaCV libraries, and add the x86 ones in the `lib` folder of the extracted multidistro archive.

1. Replace `ffmpeg-linux-x86_64.jar` with `ffmpeg-linux-x86.jar`  
2. Replace `opencv-linux-x86_64.jar` with `opencv-linux-x86.jar`

You can find the x86 libraries in the `Opencv-Natives/x86` folder.

**To run WebcamStudio latest version:**

**1)** Navigate to the multidistro archive extracted folder, where `WebcamStudio.jar` is. In a terminal, type:

```bash
$ java -jar WebcamStudio.jar
```

If you want to use `FFmpeg` backend in Ubuntu 14.04/14.10, you need to install `FFmpeg` first. Open a terminal and type:

```bash
sudo add-apt-respository ppa:mc3man/trusty-media
sudo apt-get update
sudo apt-get install ffmpeg
```

Install on OPENSUSE/FEDORA
--------------------------

### OpenSuse:

If not done yet, add Packman repository from
```
Yast2 -> Configuration -> Repository -> Add -> Community Repositories -> Pacman
```
and update all:
```bash
$ sudo zypper refresh
$ sudo zypper update
```

### Fedora:

Add RPM Fusion Free / nonFree: http://rpmfusion.org/Configuration

After adding the repository, open a terminal and with root privileges run:
```bash
$ yum update
```

### For Both:

**Step 1:** Download the fonts from: http://font.ubuntu.com/resources/  
( Download the Ubuntu Font Family › (1.5MB) )

**Step 2:** Unzip the fonts and rename the folder to whatever name like. I renamed mine to `ubuntu`.

**Step 3:** Install these fonts. Open Terminal and type the following:
```bash
$ su -
$ cd Downloads
$ cp -R ubuntu /usr/share/fonts/
```
**Step 4:** Install `gstreamer-0.10/gstreamer-1.0` with almost all plugins.

Gstreamer dependencies for WebcamStudio
---------------------------------------

### Fedora:

> **Note:** You can skip gnonlin 0.10 build because WS uses only 1.x version now, but if you need it ...

Install these packages:
```bash
$ yum install glib2-devel gstreamer-devel gstreamer-plugins-base-devel gcc
```

Compile [`gnonlin`](http://gstreamer.freedesktop.org/src/gnonlin/) from source 0.10.17 by `./configure` or `./configure --build=x86_64`

Run `make` command.

Finally run `sudo make install` to install `gnonlin`.

Copy the `gnonlin` libs from `usr/local/lib/gstreamer-0.10` to `usr/lib64/gstreamer-010` in Fedora 20 64-bit ...

I think that for 32-bit the destination is `usr/lib/gstreamer-010`.

**The following Dependences are needed:**
```
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
```

Then from a terminal type:
```bash
$ yum install gstreamer.x86_64 gstreamer-ffmpeg.x86_64 gstreamer-plugins-bad.x86_64 gstreamer-plugins-bad-free.x86_64 gstreamer-plugins-bad-free-extras.x86_64 gstreamer-plugins-bad-nonfree.x86_64 gstreamer-plugins-base.x86_64 gstreamer-plugins-espeak.x86_64 gstreamer-plugins-good.x86_64 gstreamer-plugins-ugly.x86_64 gstreamer-tools.x86_64
```

```
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
```

Then from a terminal type:
```bash
$ yum install gstreamer1.x86_64 gstreamer1-libav.x86_64 gstreamer1-plugins-bad-free.x86_64 gstreamer1-plugins-bad-free-extras.x86_64 gstreamer1-plugins-bad-freeworld.x86_64 gstreamer1-plugins-base.x86_64 gstreamer1-plugins-base-tools.x86_64 gstreamer1-plugins-good.x86_64 gstreamer1-plugins-good-extras.x86_64 gstreamer1-plugins-ugly.x86_64 gnonlin.x86_64
```

### OpenSuse:

> **Note:** You can skip `gnonlin` 0.10 configuration, because Webcam Studio uses only 1.x version now.
> If you want you can install it from:
> http://download.opensuse.org/repositories/openSUSE:/13.1/standard/x86_64/gstreamer-0_10-plugin-gnonlin-0.10.17-17.1.3.x86_64.rpm

The following Dependences are needed (installed via Yast2):
```
S | Name                                                 | Summary  | Type      
--+------------------------------------------------------+----------+-----------
i | gstreamer-0_10-plugin-gnomevfs                       | GStrea-> | package
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
i | gstreamer-0_10-utils			         | GStrea-> | package  
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
```

**Step 5:** Install `ffmpeg`, `dvgrab` and `pavucontrol`.

-------
Fedora:
-------
```bash
$ yum install ffmpeg ffmpeg-libs gstreamer-ffmpeg dvgrab pavucontrol
```
---------
OpenSuse:
---------
Install `ffmpeg` and `pavucontrol` from packman repo (Yast2).

**Step 6:** In both OpenSuse and Fedora, install `wmctrl` package to enable single desktop window capture.

**Step 7:** Extract `Webcamstudio-module-112` archive (will create `vloopback` folder):

------- 
Fedora:
-------
Install `kernel-devel`:
```bash
$ yum install kernel-headers kernel-devel
```

.. and if you have issues, refer to here: https://github.com/lwfinger/rtl8188eu/issues/98

... and look that the build link the correct current kernel (I had to reboot because my running kernel was 3.17 with buld link broken, and the correct one was to 3.18)

... and compile `webcamstudio-module-111` (Shipped in this archive `vloopback`) with `make` & `sudo make install`.

At last, `modprobe` it with `sudo modprobe webcamstudio`.

---------
OpenSuse:
---------
Install `kernel-source` and `gcc` from Yast2 ... and compile `webcamstudio-module-111` (Shipped in this archive `vloopback`) with `make` & `sudo make install`.

Later modprobe it with `sudo modprobe webcamstudio`.

**Step 8:** Install `libwebcamstudio`:

**For x64:**  
From folder `WS_libx64` (Shipped in this archive) Copy `libwebcamstudio.so` to `/usr/lib64` (or your user libs folder ...)

**For x86:**  
From folder `WS_libx86` (Shipped in this archive) Copy `libwebcamstudio.so` to `/usr/lib`

**Step 9:** SkyCam:

To use SkyCam, you have to link `gksu` command to your existing `gui-sudo` installed Package.

For example, if you have `gnomesu` or or `kdesu` installed, open a terminal and type:
```bash
$ sudo ln -s /usr/bin/gnomesu /usr/bin/gksu
```
or
```bash
$ sudo ln -s /usr/bin/kdesu /usr/bin/gksu
```

-------
Fedora:
-------
There is a `gksu-polkit` package:
```bash
$ yum install gksu-polkit
$ ln -s /usr/bin/gksu-polkit /usr/bin/gksu
```

**Step 10:** FaceDetectorAlpha (necessary step):

In x86-64bit machine you are already set-up.

In 32bit machines, to enable FaceDetector Effect, you have to remove the x86-64 **JavaCV** libraries and add the x86 ones in the `lib` folder.

The libraries are:

`ffmpeg-linux-x86_64.jar`, replace it with `ffmpeg-linux-x86.jar`.  
`opencv-linux-x86_64.jar`, replace it with `opencv-linux-x86.jar`.

You can find the x86 libraries in the `Opencv-Natives/x86` folder.

**Step 11:** Finally navigate where WebcamStudio.jar is and type from a terminal (tested to work on Opensuse 13.2):

```bash
$ java -jar WebcamStudio.jar
```

... or if this does not work (tested on Fedora 21):
```bash
$ /usr/java/default/bin/java -jar WebcamStudio.jar
```

Naturally for all streams and outputs you can't select `avconv` backend (It will be hided) if you didn't compile `avconv`.

If you want you can compile [`libAV`](http://libav.org/download.html) from source.

For WS to work, please use these libAV `./configure` settings:
```bash
$ ./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse
```

TroubleShooting:
---------------

### Opensuse:
First be sure that all `gstreamer` packages have `packman` as vendor.
To pass all packages to packman do:
```bash
$ sudo zypper dup --from packman
```

If the gstreamer backend doesn't work, maybe the codec cache is corrput.
I found the solution in this forum:

http://forums.opensuse.org/showthread.php/492716-openSUSE-13-1%28GNOME%29-codec-problem-mp4v-mpga-h264-mp4a/page3

And in particolar:

Remove `~/.cache/gstreamer-1.0/` if it exists.

Run `gst-inspect-1.0` (you may have to install `gstreamer-utils`).

This should register all installed plugins.

========================
      ARCH/MANJARO
========================
 
This is no more necessary because WS uses gnonlin 1.x, but maybe useful:

- Compile [gnonlin](http://gstreamer.freedesktop.org/src/gnonlin/) from source 0.10.17 ... 
- Once `sudo make install` is done, copy the gnonlin libs from `usr/local/lib/gstreamer` to `usr/lib/gstreamer` in Manjaro Linux ...

First of all do the following in a terminal:

```basg
$ sudo /bin/rm -f /var/lib/pacman/db.lck
 
$ sudo pacman -Syu
```
 
Then to have the correct visualization install Ubuntu fonts:
 
<Step 1> Download the fonts from: http://font.ubuntu.com/resources/
( Download the Ubuntu Font Family › (1.5MB) )
 
<Step 2> Unzip the fonts and rename them to your liking I renamed mine to "ubuntu".
 
<Step 3> Open Terminal and type the following:
$ su -
# cd Downloads
# cp -R ubuntu /usr/share/fonts/
 
<Step 4>
I used the GUI "Package Manager":
 
install "dkms" from AUR
Install "linux316-headers" from AUR
Install "gnonlin" from AUR.
Install "wmctrl" package to enable single desktop window capture.
Install "dvgrab" package to enable firewire device capture.
 
- Compile webcamstudio-module-111 (Shipped in this archive (vloopback folder)) and modprobe it:
Navigate where the vloopback folder is extracted and from a terminal type:
$ make
$ sudo make install
$ sudo modprobe webcamstudio
 
<Step 5>
For SkyCam install "gksu" from AUR.
 
<Step 6>
Install libwebcamstudio:
For x64:
From folder WS_libx64 (Shipped in the zip file) Copy libwebcamstudio.so in /usr/lib64 (or your user libs folder ...)
or
For x86:
From folder WS_libx86 (Shipped in the zip file) Copy libwebcamstudio.so in /usr/lib
 
<Step 7> FaceDetectorAlpha (necessary step)
To enable FaceDetector Effect in 32bit machines you have to remove the x86-64 JavaCV libraries and add the x86 ones in the "lib" folder.
The libraries are:
ffmpeg-linux-x86_64.jar replace with ffmpeg-linux-x86.jar
opencv-linux-x86_64.jar replace with opencv-linux-x86.jar
You can find the x86 libraries in the "Opencv-Natives/x86" folder.
 
Finally navigate where WebcamStudio.jar is and type from a terminal:
$ java -jar WebcamStudio.jar
 
Naturally for all streams and outputs you can't select "avconv" backend (It will be hided) if you didn't compile Avconv.
If you want you can compile libAV from sources. "http://libav.org/download.html"
For WS to work, please use this libAV ./configure settings:
 
$./configure --enable-gpl --enable-nonfree --enable-pthreads --enable-libx264 --enable-libfaac --enable-libmp3lame --enable-version3 --enable-librtmp --enable-x11grab --enable-libpulse
 

===
Have a nice day.
karl.
