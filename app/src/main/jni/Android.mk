LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := base
LOCAL_SRC_FILES := adbi/libbase.a
LOCAL_EXPORT_C_INCLUDES := adbi
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := MagicBox
LOCAL_CFLAGS := -w
LOCAL_LDLIBS := -llog -lz -lm
LOCAL_SHARED_LIBRARIES := dl
LOCAL_STATIC_LIBRARIES := base
#	-lD:\AndroidWS\cbt_crack\app\src\main\jniLibs\armeabi\libcocos2djs.so
LOCAL_SRC_FILES := com_bigzhao_jianrmagicbox_CppInterface.cpp
include $(BUILD_SHARED_LIBRARY)
