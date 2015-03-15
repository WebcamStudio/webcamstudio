

# Introduction #

Here anyone of the developers can add his ideas about the architecture of WebcamStudio, its GUI, plugins etc. and discuss all this in the comments below.


# GUI #
  1. When the user needs to add a new source like webcam for example, now clicking the button, adds all sources that can be found under _/dev/video`*`_...
    * A drop-down list to be offered before the user adds any such source is needed
    * We need to have in mind that at this level of sources, often there is a DVB or any other TV card listed under _/dev/video`*`_ i.e. we need to add additional functionality for commanding such devices, so the user can choose a channel from his DVB etc card
  1. There should be a check on what the user has as installed applications (between gstreamer/libav and ffmpeg) and depending on the result the user to be able to select the app used for every source or collection...
  1. The input method for setting stream attributes is a little bit tricky. We have to find an alternative user friendly input method for things like Width, Height, etc... and a better design for the user interface.

More content to be added...

# Plugins #

Content to be added...

# Misc #

Content to be added...