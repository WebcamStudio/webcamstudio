# Introduction #
Here are discussed the Linux specifics about WebcamStudio.

# Details #
  * GStreamer, ffmpeg and libav are available from the package manager of most of the contemporary distributions.
  * The virtual webcam module is the thing that is a little harder to deploy.
    * WebcamStudio is using a forked module of [v4l2loopback](https://github.com/umlaeute/v4l2loopback) (previously vloopback) renamed to webcamstudio.
    * The problem with the recompiling the module for each kernel is fixed for versions 0.60+ on Debian like systems, since it uses [DKMS](http://en.wikipedia.org/wiki/Dynamic_Kernel_Module_Support)
  * The original [v4l2loopback](https://github.com/umlaeute/v4l2loopback) kernel module could be used since versions 0.60+ on Debian like systems
  * On some distributions GStreamer works much better and faster than ffmpeg/libav on the same system (for example this is the case with Gentoo). This should be further investigated.