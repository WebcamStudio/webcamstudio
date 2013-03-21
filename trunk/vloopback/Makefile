KERNEL_VERSION	?= $(shell uname -r)
KERNEL_DIR	?= /lib/modules/$(KERNEL_VERSION)/build

PWD		:= $(shell pwd)

obj-m	:= webcamstudio.o

.PHONY: all webcamstudio install clean

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
