#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <libevdev/libevdev.h>
#include <libevdev/libevdev-uinput.h>
// #include <mutex>

class VirtualMouse {
public:
	struct libevdev_uinput* m_uinput = nullptr;
	// std::mutex m_mouseMutex;
public:
	VirtualMouse() {}
	~VirtualMouse() {
		libevdev_uinput_destroy(m_uinput);
	}

	int Init() {
		struct libevdev* dev = libevdev_new();

		libevdev_set_name(dev, "Virtual Joystick");

		printf("criou\n");

		libevdev_enable_property(dev, INPUT_PROP_BUTTONPAD);

		// libevdev_enable_event_type(dev, EV_REL);
		// libevdev_enable_event_code(dev, EV_REL, REL_X, nullptr);
		// libevdev_enable_event_code(dev, EV_REL, REL_Y, nullptr);
		// libevdev_enable_event_code(dev, EV_REL, REL_WHEEL, nullptr);

		libevdev_enable_event_type(dev, EV_KEY);
		libevdev_enable_event_code(dev, EV_KEY, BTN_0, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_1, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_2, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_3, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_4, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_5, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_6, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_7, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_8, nullptr);
		libevdev_enable_event_code(dev, EV_KEY, BTN_9, nullptr);

		printf("hardware\n");

		int r = libevdev_uinput_create_from_device(dev, LIBEVDEV_UINPUT_OPEN_MANAGED, &m_uinput);

		printf("free\n");

		libevdev_free(dev);

		printf("return\n");

		return r;
	}

	void Move(int rx, int ry) {
		// std::lock_guard<std::mutex> guard(m_mouseMutex);
		libevdev_uinput_write_event(m_uinput, EV_REL, REL_X, rx);
		libevdev_uinput_write_event(m_uinput, EV_REL, REL_Y, ry);
		libevdev_uinput_write_event(m_uinput, EV_SYN, SYN_REPORT, 0);
	}

	void Scroll(int rs) {
		// std::lock_guard<std::mutex> guard(m_mouseMutex);
		libevdev_uinput_write_event(m_uinput, EV_REL, REL_WHEEL, rs);
		libevdev_uinput_write_event(m_uinput, EV_SYN, SYN_REPORT, 0);
	}

	void Click(int btn, bool isDown) {
		// std::lock_guard<std::mutex> guard(m_mouseMutex);
		libevdev_uinput_write_event(m_uinput, EV_KEY, btn, isDown);
		libevdev_uinput_write_event(m_uinput, EV_SYN, SYN_REPORT, 0);
	}
};

VirtualMouse g_mouse;

int main() {
	char key[200];

	printf("iniciou\n");

	// must init mouse before we drop permissions
	int retint = g_mouse.Init();
	if (retint != 0) {
		// std::cerr << "couldn't init mouse!" << std::endl;
		printf("error\n");
		printf("%s\n", strerror(abs(retint)));
		return -1;
	}

	printf("esperando tecla");
	scanf("%s",key);

	g_mouse.Click(1, true);
	g_mouse.Click(1, false);
}