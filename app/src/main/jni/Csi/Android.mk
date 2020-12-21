LOCAL_PATH := $(call my-dir)
### JNICsi
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
	radiotap.c \
	nex_dot11rt.c \
	makecsiparams/bcmwifi_channels.c \
	JNICsi.c

LOCAL_LDLIBS := -llog

LOCAL_MODULE := JNICsi
LOCAL_CFLAGS += -DHAVE_CONFIG_H
LOCAL_CFLAGS += -D_U_="__attribute__((unused))"
# LOCAL_CFLAGS += -Werror
LOCAL_CFLAGS += -I./makecsiparams
LOCAL_CFLAGS += -DVERSION=\"$(GIT_VERSION)\" -DD11AC_IOTYPES -DCHANSPEC_NEW_40MHZ_FORMAT
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
include $(BUILD_SHARED_LIBRARY)

### csiutil
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
    nex_dot11rt.c \
    mcutils.c \
	csiutil.c

LOCAL_STATIC_LIBRARIES += libpcap
LOCAL_STATIC_LIBRARIES += libargp

LOCAL_MODULE := csiutil
LOCAL_CFLAGS += -DHAVE_CONFIG_H
LOCAL_CFLAGS += -D_U_="__attribute__((unused))"
# LOCAL_CFLAGS += -Werror
LOCAL_CFLAGS += -DVERSION=\"$(GIT_VERSION)\" -DD11AC_IOTYPES -DCHANSPEC_NEW_40MHZ_FORMAT
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
include $(BUILD_EXECUTABLE)

### injectutil
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
    mcutils.c \
	injectutil.c

LOCAL_STATIC_LIBRARIES += libargp

LOCAL_MODULE := injectutil
LOCAL_CFLAGS += -DHAVE_CONFIG_H
LOCAL_CFLAGS += -D_U_="__attribute__((unused))"
# LOCAL_CFLAGS += -Werror
LOCAL_CFLAGS += -DVERSION=\"$(GIT_VERSION)\" -DD11AC_IOTYPES -DCHANSPEC_NEW_40MHZ_FORMAT
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
include $(BUILD_EXECUTABLE)

### makecsiparams
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
    makecsiparams/bcmwifi_channels.c \
	makecsiparams/makecsiparams.c

LOCAL_MODULE := makecsiparams
LOCAL_CFLAGS += -DHAVE_CONFIG_H
LOCAL_CFLAGS += -D_U_="__attribute__((unused))"
LOCAL_CFLAGS += -I./makecsiparams
# LOCAL_CFLAGS += -Werror
LOCAL_CFLAGS += -DVERSION=\"$(GIT_VERSION)\" -DD11AC_IOTYPES -DCHANSPEC_NEW_40MHZ_FORMAT
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE := libpcap
LOCAL_SRC_FILES := $(LOCAL_PATH)/../libpcap/local/armeabi/libpcap.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../libpcap
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := libargp
LOCAL_SRC_FILES := $(LOCAL_PATH)/../libargp/local/armeabi/libargp.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../libargp
include $(PREBUILT_STATIC_LIBRARY)