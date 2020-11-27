GIT_VERSION := $(shell git describe --abbrev=4 --dirty --always --tags)

all: libs/armeabi/csicollect

libs/armeabi/csicollect: Android.mk ../libpcap/local/armeabi/libpcap.a
	$(NDK_ROOT)/ndk-build NDK_APPLICATION_MK=`pwd`/Application.mk NDK_APP_OUT=. TARGET_PLATFORM=android-21 APP_ABI=armeabi GIT_VERSION=$(GIT_VERSION)

../libpcap/local/armeabi/libpcap.a: FORCE
	cd ../libpcap && make

clean:
	rm -Rf libs
	rm -Rf local

FORCE:

