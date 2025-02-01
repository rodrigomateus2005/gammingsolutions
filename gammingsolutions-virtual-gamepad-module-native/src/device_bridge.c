#include <jni.h>
#include <stdio.h>
#include <device/device.c>
#include <br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge.h>

// struct gamepad_struct
// {
//     struct libevdev *evdev;
//     struct libevdev_uinput *uinput;
// };

// struct gamepad_struct get_game_pad_struct(JNIEnv *env, jobject gamepad)
// {
//     struct gamepad_struct retorno;

//     jclass cls_foo = (*env)->GetObjectClass(env, gamepad);

//     jmethodID mid_get_evdev = (*env)->GetMethodID(env, cls_foo, "getEvdev", "()J");
//     jmethodID mid_get_uinput = (*env)->GetMethodID(env, cls_foo, "getUinput", "()J");

//     // then call them.
//     struct libevdev *evdev = (struct libevdev *)(*env)->CallObjectMethod(env, gamepad, mid_get_evdev);
//     struct libevdev_uinput *uinput = (struct libevdev_uinput *)(*env)->CallObjectMethod(env, gamepad, mid_get_uinput);

//     retorno.evdev = evdev;
//     retorno.uinput = uinput;

//     return retorno;
// }

JNIEXPORT jobject JNICALL Java_br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge_createVirtualGamepad(JNIEnv *env, jobject)
{
    struct libevdev *evdev = create_dev();
    struct libevdev_uinput *uinput = create_uinput(evdev);

    jclass cls = (*env)->FindClass(env, "br/com/gammingsolution/virtualgamepad/model/GamepadNative");
    jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(JJ)V");
    jobject object = (*env)->NewObject(env, cls, constructor, evdev, uinput);

    return object;
}

JNIEXPORT void JNICALL Java_br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge_destroyVirtualGamepad(JNIEnv *env, jobject, jlong evdev, jlong uinput)
{
    // struct gamepad_struct gamepad_local = get_game_pad_struct(env, gamepad);

    destroy_gamepad((struct libevdev *) evdev, (struct libevdev_uinput *) uinput);
}

JNIEXPORT void JNICALL Java_br_com_gammingsolution_virtualgamepad_VirtualGamepadBridge_sendEvent(JNIEnv *env, jobject, jlong uinput, jint type, jint code, jint value)
{
    send_event((struct libevdev_uinput *) uinput, type, code, value);
}
