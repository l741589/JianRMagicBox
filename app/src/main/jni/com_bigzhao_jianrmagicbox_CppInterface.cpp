#include"com_bigzhao_jianrmagicbox_CppInterface.h"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdarg.h>
#include <dlfcn.h>
#include <android/log.h>
#include "adbi/base.h"
#include "adbi/hook.h"

#ifndef LOG_TAG
#define LOG_TAG "ANDROID_LAB"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#endif

static struct hook_t eph;

typedef void*(*pf_getter)();

void* f(){
    LOGE("hooked");
    hook_precall(&eph);
    void *ret=(reinterpret_cast<pf_getter>((void*)eph.orig))();
    hook_postcall(&eph);
    return ret;
}

static void my_log(char *msg)
{
	LOGE("%s", msg);
}

void a(){
    LOGE("a");
}

class A{
public:
     void a();
    virtual void b();
};

void A::a(){
        LOGE("A.a");
    }
void A::b(){
        LOGE("A.b");
    }

JNIEXPORT void JNICALL Java_com_bigzhao_jianrmagicbox_CppInterface_init(JNIEnv *env, jobject obj){
    LOGE("init");
    set_logfunction((void*)my_log);
    A*aa=new A();
    a();
    aa->a();
    aa->b();
    hook(&eph, getpid(), "libcocos2dcpp.", "_ZN7cocos2d8Director11getInstanceEv", (void*)f, (void*)f);
}
