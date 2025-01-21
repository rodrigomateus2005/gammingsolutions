#include <jni.h>
#include <stdio.h>
#include <device/device.c>
#include <br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge.h>

JNIEXPORT jobject JNICALL Java_br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge_createVirtualGamepad(JNIEnv *env, jobject)
{
    struct libevdev *evdev = create_dev();
    struct libevdev_uinput *uinput = create_uinput(evdev);
    
    jclass cls = (*env)->FindClass(env, "br/com/gammingsolution/virtualgamepad/model/GamepadNative");
    jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(JJ)V");
    jobject object = (*env)->NewObject(env, cls, constructor, evdev, uinput);

    return object
}