# LocTag App on Nexus 5
An App that can realize injection, sniff, csi extaction (based on *nexmon* project)

## Summary
- **Project structure:**
    - Userspace Android APP (Java)
    - *injectutil*, *csiutil* tools runing on linux with su privilege (C language)
    - JNI codes (C language)

- **Ndk**
    - android-ndk-r11c

- **Project settings**
    - Android studio 4.0.1
    - compileSdkVersion 28
    - buildToolsVersion "28.0.3"
    - minSdkVersion 23
    - targetSdkVersion 23
    - abiFilters "armeabi"

## Build

### build JNI code
Java file is in:
```
app\src\main\java\top\tjunet\loctag\jni\CsiUtils.java
```
generate header file for the native methods in CsiUtils.java:
```
app\src\main\java> javah top.tjunet.loctag.jni.CsiUtils
```
The above command will generate file:
```
app\src\main\jni\Csi\top_tjunet_loctag_jni_CsiUtils.h
```
implement native method's interfaces in JNICsi.c which is in:
```
app\src\main\jni\Csi\JNICsi.c
```
make *.mk files for ndk-build. require:
```
app\src\main\jni\Csi\Android.mk
app\src\main\jni\Csi\Application.mk
```
```
app\src\main\jni\Csi\Android.mk
app\src\main\jni\Csi\Application.mk
app\src\main\jni\Csi\top_tjunet_loctag_jni_CsiUtils.h
app\src\main\jni\Csi\JNICsi.c
... <some other documents required by JNICsi.c>
```
then build JNICsi.c using ndk-build:
```
app\src\main\jni\Csi> ndk-build NDK_APPLICATION_MK=Application.mk NDK_APP_OUT=. TARGET_PLATFORM=android-23 APP_ABI=armeabi
```
Finally, you can get .so library file  in the `./lib/armeabi/` or `./local/armeabi/` directory

## memo
Push files from host to android device.
```
adb push libs/armeabi/csiutil /sdcard/nextools/
adb push libs/armeabi/injectutil /sdcard/nextools/
adb push libs/armeabi/makecsiparams /sdcard/nextools/
mount -o rw,remount /system
cp /sdcard/nextools/csiutil /system/bin/csiutil
cp /sdcard/nextools/injectutil /system/bin/injectutil
cp /sdcard/nextools/makecsiparams /system/bin/makecsiparams
chmod +x /system/bin/csiutil /system/bin/injectutil /system/bin/makecsiparams
```
replace and reload firmware
```
mount -o rw,remount /system
cp /sdcard/nextools/fw_bcmdhd.bin.csi /system/vendor/firmware/fw_bcmdhd.bin
# cp /sdcard/nextools/fw_bcmdhd.bin.nex /system/vendor/firmware/fw_bcmdhd.bin
# cp /sdcard/nextools/fw_bcmdhd.bin.orig /system/vendor/firmware/fw_bcmdhd.bin
ifconfig wlan0 down
ifconfig wlan0 up
```
injectutil usage
```
nexutil -Iwlan0 -m1 -k1/20
injectutil -Iwlan0 -n1 -p1000 -d0 -r0xc0000000 -f/sdcard/dot11pkts/mgt-beacon-example.bin -l4
```
use csiutil sniff packets with standalone mode
```
nexutil -Iwlan0 -m2 -k1/20
LD_PRELOAD=libfakeioctl.so csiutil -m2 -f"ether src 00:11:22:33:44:55" -l0 -t1 -d1 -n100
```
use csiutil extract csi with standalone mode
```
nexutil -Iwlan0 -m1
nexutil -Iwlan0 -s500 -b -l34 -vndABEQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==
csiutil -m2 -f"dst port 5500" -l0 -t2 -d1 -n%d
```
