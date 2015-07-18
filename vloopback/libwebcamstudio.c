/*
 * How to test the webcamstudio virtual video device:
 *   Feed the video device with data according to the settings specified below:
 *   size, pixelformat, etc.
 *   For instance, you can try the default settings with this command:
 *       mencoder video.avi -ovc raw -nosound -vf scale=640:480,format=yuy2 -o /dev/video1
 *
 *   Test the video in your favourite viewer, for instance:
 *       luvcview -d /dev/video1 -f yuyv
 */

#include <linux/videodev2.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <assert.h>


int open_device(char* dev, int width, int height, int pixFormat){
	struct v4l2_capability vid_caps;
	struct v4l2_format vid_format;

	memset(&vid_format, 0, sizeof(vid_format));
	vid_format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
	vid_format.fmt.pix.width = width;
	vid_format.fmt.pix.height = height;

	switch (pixFormat) {
		case 1:
			vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
			vid_format.fmt.pix.bytesperline = (width * 3);
			vid_format.fmt.pix.sizeimage = (width * height * 3);
			break;

		// UYVY has only half the color resolution of RGB, but there is less confusion
		// surrounding its byte ordering
		case 2:
			vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_UYVY;
			vid_format.fmt.pix.bytesperline = (width * 2);
			vid_format.fmt.pix.sizeimage = (width * height * 2);
			break;

		// Adding BGR24 as format #3 in anticipation of Chrome hopefully adopting it in the future
		// (After all, BGR24 is what they actually implemented, they just call it RGB24)
		case 3:
			vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_BGR24;
			vid_format.fmt.pix.bytesperline = (width * 3);
			vid_format.fmt.pix.sizeimage = (width * height * 3);
			break;

		default:
			// Unknown pixFormat, bail out.
			return -1;
	}

	vid_format.fmt.pix.field = V4L2_FIELD_NONE;
	vid_format.fmt.pix.priv = 0;
	vid_format.fmt.pix.colorspace = V4L2_COLORSPACE_JPEG;

	int fdwr = open(dev, O_RDWR);
	int ret_code = ioctl(fdwr, VIDIOC_QUERYCAP, &vid_caps);
	ret_code = ioctl(fdwr, VIDIOC_S_FMT, &vid_format);
	return fdwr;
}


int close_device(int devfd) {
	close(devfd);
	return 0;
}


int writeData(int devfd, __u8 data[], int length) {
	int retValue = write(devfd, data, length);
	return retValue;
}
