#include <stdio.h>
#include <stdbool.h>
#include <libevdev/libevdev.h>
#include <libevdev/libevdev-uinput.h>

bool check(int code) {
    if (code < 0) {
        // fprintf(stderr, "Failed to init libevdev (%s)\n", strerror(-code));
        return false;
    }

    return true;
}

struct libevdev_uinput *create_uinput(struct libevdev *evdev)
{
    struct libevdev_uinput *uinput;
    if (!check(libevdev_uinput_create_from_device(evdev, LIBEVDEV_UINPUT_OPEN_MANAGED, &uinput))) return NULL;

    return uinput;
}

void init_dev(struct libevdev *evdev)
{
    libevdev_set_name(evdev, "Generic X-Box pad");
    libevdev_set_id_vendor(evdev, 0x2f24);
    libevdev_set_id_product(evdev, 0x00ba);
    libevdev_set_id_version(evdev, 0x110);
    libevdev_set_id_bustype(evdev, BUS_USB);
}

bool init_abs_events(struct libevdev *evdev)
{
    struct input_absinfo absinfo = {
        .value = 0,
        .minimum = 0,
        //.maximum = 12372,
        .maximum = 32767,
        .fuzz = 0,
        .flat = 0,
        .resolution = 40};

    if (!check(libevdev_enable_event_type(evdev, EV_ABS))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_X, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_Y, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_Z, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_RX, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_RY, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_RZ, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_BRAKE, &absinfo))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_ABS, ABS_THROTTLE, &absinfo))) return false;

    return true;
}

bool init_key_events(struct libevdev *evdev)
{
    if (!check(libevdev_enable_event_type(evdev, EV_KEY))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_A, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_B, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_Y, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_X, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_TL, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_TR, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_TL2, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_TR2, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_SELECT, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_START, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_THUMBL, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_THUMBR, NULL))) return false;
    if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_MODE, NULL))) return false;

    // if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_DPAD_UP, NULL))) return false;
    // if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_DPAD_DOWN, NULL))) return false;
    // if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_DPAD_LEFT, NULL))) return false;
    // if (!check(libevdev_enable_event_code(evdev, EV_KEY, BTN_DPAD_RIGHT, NULL))) return false;

    return true;
}

struct libevdev *create_dev()
{
    struct libevdev *evdev = libevdev_new();

    init_dev(evdev);
    if (!init_abs_events(evdev)) return NULL;
    if (!init_key_events(evdev)) return NULL;

    return evdev;
}

void send_event(struct libevdev_uinput *uinput, unsigned int type, unsigned int code, int value) {
    libevdev_uinput_write_event(uinput, type, code, value);
    libevdev_uinput_write_event(uinput, EV_SYN, SYN_REPORT, 0);
}

void destroy_gamepad(struct libevdev *evdev, struct libevdev_uinput *uinput) {
    libevdev_uinput_destroy(uinput);
    libevdev_free(evdev);
}