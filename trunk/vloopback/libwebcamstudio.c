/* 
 * How to test v4l2loopback:
 * 1. launch this test program (even in background), it will initialize the
 *    loopback device and keep it open so it won't loose the settings.
 * 2. Feed the video device with data according to the settings specified
 *    below: size, pixelformat, etc.
 *    For instance, you can try the default settings with this command:
 *    mencoder video.avi -ovc raw -nosound -vf scale=640:480,format=yuy2 -o /dev/video1
 *    TODO: a command that limits the fps would be better :)
 *
 * Test the video in your favourite viewer, for instance:
 *   luvcview -d /dev/video1 -f yuyv
 */

#include <linux/videodev2.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <assert.h>

int open_device(char* dev,int width,int height,int pixFormat){
	struct v4l2_capability vid_caps;
	struct v4l2_format vid_format;
	int fdwr = open(dev, O_RDWR);
	int ret_code = ioctl(fdwr, VIDIOC_QUERYCAP, &vid_caps);
	memset(&vid_format, 0, sizeof(vid_format));
	vid_format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
	vid_format.fmt.pix.width = width;
	vid_format.fmt.pix.height = height;
	switch(pixFormat){
		case 1:
		        vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
			vid_format.fmt.pix.bytesperline = width *3;
	break;
		case 2:
		        vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_UYVY;
			vid_format.fmt.pix.bytesperline = width *2;
			break;
	}
	vid_format.fmt.pix.sizeimage = vid_format.fmt.pix.bytesperline*height;
	vid_format.fmt.pix.field = V4L2_FIELD_NONE;
	vid_format.fmt.pix.priv = 0;
	vid_format.fmt.pix.colorspace = V4L2_COLORSPACE_JPEG;
	ret_code = ioctl(fdwr, VIDIOC_S_FMT, &vid_format);
	return fdwr;
}
int close_device(int devfd){
	close(devfd);
	return 0;
}
int writeData(int devfd,__u8 data[],int length){
	int retValue = write(devfd, data, length);
	return retValue;
}


