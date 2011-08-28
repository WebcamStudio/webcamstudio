KERNEL_VERSION	:= `uname -r`
KERNEL_DIR	:= /lib/modules/$(KERNEL_VERSION)/build

PWD		:= $(shell pwd)

obj-m		:= webcamstudio.o


MODULE_OPTIONS = devices=2

all: webcamstudio
webcamstudio:
	@echo "Building webcamstudio driver..."
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) modules
install:
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) modules_install
	depmod -ae
clean:
	rm -f *~
	rm -f Module.symvers Module.markers modules.order
	$(MAKE) -C $(KERNEL_DIR) M=$(PWD) clean

modprobe: webcamstudio
	modprobe videodev
	-rmmod webcamstudio
	insmod ./webcamstudio.ko $(MODULE_OPTIONS)

