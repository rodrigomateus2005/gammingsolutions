#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <device/device.c>

struct libevdev *evdev;
struct libevdev_uinput *uinput;

void destroy_gamepad_test() {
    destroy_gamepad(evdev, uinput);
}

void intHandler(int dummy) {
    printf("destroyed\n");
    destroy_gamepad_test();
}

// void init_signals(){
//     struct sigaction sigact;
//     sigact.sa_handler = intHandler;
//     sigemptyset(&sigact.sa_mask);
//     sigact.sa_flags = 0;
//     sigaction(SIGINT, &sigact, (struct sigaction *)NULL);
// }

int main() {

    // signal(SIGINT, intHandler);
    // signal(SIGKILL, intHandler);
    // signal(SIGTERM, intHandler);
    struct sigaction sa = { .sa_handler = intHandler /* default sa_mask and sa_flags */ };
    sigaction(SIGINT, &sa, NULL);
    sigaction(SIGKILL, &sa, NULL);
    sigaction(SIGTERM, &sa, NULL);

    printf("Starting new dev\n");
    evdev = create_dev();    

    printf("Starting uinput dev\n");
    uinput = create_uinput(evdev);

    // while (true)
    // {
        int  number;
        scanf("%d", &number);

        printf("Sending events\n");
        // send_event(uinput, EV_KEY, BTN_SOUTH, 1)
        send_event(uinput, 0x130, 1);
    // }

    destroy_gamepad_test();
}