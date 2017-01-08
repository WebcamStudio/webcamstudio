KERNEL_VERSION	?= $(shell uname -r)
KERNEL_DIR	?= /lib/modules/$(KERNEL_VERSION)/build

PWD		:= $(shell pwd)

obj-m	:= webcamstudio.o

.PHONY: all webcamstudio install clean libwebcamstudio clean-libwebcamstudio

all: webcamstudio.ko
webcamstudio: webcamstudio.ko
webcamstudio.ko:
	@echo "Building webcamstudio kernel module..."
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) modules
install:
	@echo "Installing webcamstudio kernel module..."
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) modules_install
	depmod -a
clean:
	@echo "Cleaning webcamstudio kernel module folder..."
	$(RM) -f *~
	$(RM) -f Module.symvers Module.markers modules.order
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) clean
libwebcamstudio:
	@echo "Building libwebcamstudio helper..."
	$(CC) \
		$(CPPFLAGS) $(CFLAGS) \
		-fPIC \
		-c -o libwebcamstudio.o libwebcamstudio.c
	$(CC) \
		$(CPPFLAGS) $(CFLAGS) $(LDFLAGS) \
		-fPIC \
		-shared -Wl,-soname,libwebcamstudio.so \
		-o libwebcamstudio.so.1.0.1  \
		libwebcamstudio.o
	$(shell ln -s libwebcamstudio.so.1.0.1 libwebcamstudio.so)
clean-libwebcamstudio:
	@echo "Cleaning libwebcamstudio helper folder..."
	$(RM) -f libwebcamstudio.o libwebcamstudio.so*
