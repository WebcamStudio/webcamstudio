

# Introduction #
Here you can find some answers to frequently asked questions about WebcamStudio.

# Definitions #
## What is V4L2 ? ##
In the Linux world, the input video is managed using the V4L and V4L2 frameworks. Actually the Video For Linux (V4L) is an API that provides unified access to various video capturing devices, such as TV tuners, USB web cameras, etc. The V4L2 interface, is a successor of V4L, which fixes bugs found in V4L, and supports a wider range of devices.For more info look at [The Intro to V4L2 Article](http://www.linuxfordevices.com/c/a/Linux-For-Devices-Articles/Intro-to-V4L2/) or at [The Kernel Documentation](https://www.kernel.org/doc/Documentation/video4linux/v4l2-framework.txt).

## What is a VLoopback device ? ##
A VLoopback device is a virtual device that provides a way to simulate a video input in Linux.

When you connect a webcam to a linux computer, a special device is created in the _/dev_ folder, generally the name is _/dev/video0_. If you have more of those devices, then the names will increase in numbering like _/dev/video1_, _/dev/video2_, etc...

A VLoopback device is a module loaded dynamically in the kernel to provide a virtual video input device. To make it work, this device has to be fed by a video stream with a predefined format: size, frame, rate and colour format.

For example: The original vloopback module from the Motion project, is a V4L device supporting a wide range of sizes and framerates, but only in RGB format.

[Another project](https://github.com/umlaeute/v4l2loopback) ported that code to V4L2Loopback to provide compatibility with V4L2 hardware. It's a bit more complex, but instead of having 2 virtual device (video1 input, video2 output), it creates only one device that can read and export a video stream.

_**Note: Since those devices are virtual, if your browser is started before WebcamStudio for example, it is possible that Flash will not recognize those device as video input.  Make sure to start WebcamStudio first, then your browser (or any other software that uses the virtual devices) so they will be detected properly.**_

## What is Chroma Key and Chroma Keying ? ##
Chroma Keying is a way of selecting a colour in an image and then replacing that colour with a transparent one.  For example, the Weather guy is in front of a green wall, and all greens are removed to be replaced by a background image/video.


# General Questions #
To be written...


# Installation #
To be written...


# Usage #
## My virtual webcamera is not working with Adobe Flash, Skype or some other application in Linux... ##
> _"When visiting any website that has flash, I expect the flash applet to request my permission to access the video devices on my system.  I do get the request for permission, and grant them. The problem is that I only see my integrated webcam device, and not the virtual one."_

> _"The virtual webcam does not appear in Skype's test window, nor does it appear in the preview box in-call, nor can the person being called see the video."_

This is not because of the virtual webcam driver. The problem usually is because the affected application uses the V4L framework instead of the newer one - V4L2.
There is an easy way to fix this.

You need to use the [libv4l](http://linuxtv.org/downloads/v4l-utils/) compatibility layer. You do it by installing the corresponding package from your distro's package manager and then write a script to start the affected application after pre-loading the v4l1compat.so library.

For example if you are in Ubuntu amd64 and you have problems with Skype:
  * Install the libv4l-0 package
  * Open terminal as root, and create a file named (for example) _/usr/bin/skype-compat_ with the following content:
```sh

#!/bin/bash
LD_PRELOAD="/usr/lib/i386-linux-gnu/libv4l/v4l1compat.so" /usr/bin/skype
```
  * Make the file executable and then start Skype by using the above script

_**Note:** It is important to know if the affected binary is x86 or an 64. Since Skype is x86 binary, you will need the x86 compat library, but if you use Ubuntu amd64, you will need to install the [libv4l-0:i386](http://packages.ubuntu.com/quantal/libv4l-0) and the [libv4lconvert0:i386](http://packages.ubuntu.com/quantal/libv4lconvert0) manually downloading it from the links provided._

For more info look at [issue 81](https://code.google.com/p/webcamstudio/issues/detail?id=81) ([post 7](https://code.google.com/p/webcamstudio/issues/detail?id=81#c7)) and [issue 20](https://code.google.com/p/webcamstudio/issues/detail?id=20). You may also read a [Dedicated to the topic tutorial for Mint](http://community.linuxmint.com/tutorial/view/219).

## When I use the v4l2loopback-dkms module instead of the webcamstudio-dkms module, my applications do not recognize the virtual cam at all... ##
To fix this issue you need to start feeding data to the virtual webcamera in WebcamStudio before you start any application that will use the virtual webcam.
The problem is explained in the [definitions section of this FAQ](FAQ#Definitions.md) - the VLoopback device.