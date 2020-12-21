GIT_VERSION := $(shell git describe --abbrev=4 --dirty --always --tags)

all: libs/armeabi/csiutil libs/armeabi/injectutil libs/armeabi/makecsiparams

libs/armeabi/csiutil libs/armeabi/injectutil libs/armeabi/makecsiparams: Android.mk ../libpcap/local/armeabi/libpcap.a ../libargp/local/armeabi/libargp.a
	$(NDK_ROOT)/ndk-build NDK_APPLICATION_MK=`pwd`/Application.mk NDK_APP_OUT=. TARGET_PLATFORM=android-23 APP_ABI=armeabi GIT_VERSION=$(GIT_VERSION)

../libpcap/local/armeabi/libpcap.a: FORCE
	cd ../libpcap && make

../libargp/local/armeabi/libargp.a: FORCE
	cd ../libargp && make

install: libs/armeabi/csiutil libs/armeabi/injectutil libs/armeabi/makecsiparams
	adb push libs/armeabi/csiutil /sdcard/
	adb push libs/armeabi/injectutil /sdcard/
	adb push libs/armeabi/makecsiparams /sdcard/
	adb shell 'su -c "mount -o rw,remount /system"'
	adb shell 'su -c "cp /sdcard/nextools/csiutil /system/bin/csiutil"'
	adb shell 'su -c "cp /sdcard/nextools/injectutil /system/bin/injectutil"'
	adb shell 'su -c "cp /sdcard/nextools/makecsiparams /system/bin/makecsiparams"'
	adb shell 'su -c "chmod +x /system/bin/csiutil"'
	adb shell 'su -c "chmod +x /system/bin/injectutil"'
	adb shell 'su -c "chmod +x /system/bin/makecsiparams"'

clean:
	rm -Rf libs
	rm -Rf local

FORCE:

