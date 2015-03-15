# Introduction #

WebcamStudio can be several things. But the main goal is to provide an easy to use software to the users, so they can build their own TV studio on their computer.


# Details #

Too many software developers are forgetting that in the end, a regular user will want to use their software. Even if the command line can be fun and powerful, it is much easier to simply point-and-click to execute some tasks.

So with that in mind, a regular user must be able to do this:

  1. Download the package
  1. Install the package by double-clicking on it
  1. Start the application from the menu
  1. Use it right away without having to compile anything

The tricky part is that a virtual driver must be installed to provide the virtual webcam. And this driver must me recompiled each time there is a kernel update to ensure that WebcamStudio will be ready to use for all local users.  It seems that a user needs to be part of the "video" group to make sure he has access in read/write mode to that device. Already, in the debian package of versions 0.60+, the scripting has been done to automate that process using [DKMS](http://en.wikipedia.org/wiki/Dynamic_Kernel_Module_Support).

So, a GUI is needed to make things easier. The major problem is how to fit all that information without cluttering the interface...  Here is the items or modules that are required:

  * The output control (pixel format, output size, etc...)
  * The loaded sources ready to be used
  * The controls for each source to apply special effects, resizing, etc...
  * A Layout manager so the user can switch from one layout to another on a single click
  * A source browser that makes it easy for the user to add new sources to the current studio
  * Error and information messages that must show up when something happens
  * Having a smooth experience when something is loading
  * Having a preview of what is currently happening (small previews of the sources)
  * Having some stats to help control the bandwidth usage in some cases

For a user, WebcamStudio should be easy to use and understand.
No manual configuration files editing (for the most common cases), no console power user needed...
It should be easy and slick!