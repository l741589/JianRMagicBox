LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_SRC_FILES := \
	E:\AndroidWS\JianRMagicBox\app\src\main\jni\placeholder.cpp \

LOCAL_C_INCLUDES += E:\AndroidWS\JianRMagicBox\app\src\main\jni
LOCAL_C_INCLUDES += E:\AndroidWS\JianRMagicBox\app\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
