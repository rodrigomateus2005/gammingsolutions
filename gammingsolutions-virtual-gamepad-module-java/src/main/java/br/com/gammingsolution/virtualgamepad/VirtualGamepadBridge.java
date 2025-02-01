package br.com.gammingsolution.virtualgamepad;

import br.com.gammingsolution.virtualgamepad.model.GamepadNative;

import java.io.*;

public class VirtualGamepadBridge {

    static {
        // System.loadLibrary("libvirtualgamepad_bridge"); // libvirtualgamepad_bridge.dll (Windows) or libvirtualgamepad_bridge.so (Unixes)
        loadJarLibrary("libvirtualgamepad_bridge.so");
    }

    private static void loadJarLibrary(String name) {
        try {
            InputStream in = VirtualGamepadBridge.class.getClassLoader().getResourceAsStream(name);
            byte[] buffer = new byte[1024];
            int read = -1;
            File temp = null;

            temp = File.createTempFile(name, "");
            FileOutputStream fos = new FileOutputStream(temp);

            while((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();

            System.load(temp.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public native GamepadNative createVirtualGamepad();
    public void destroyVirtualGamepad(GamepadNative gamepad) {
        destroyVirtualGamepad(gamepad.getEvdev(), gamepad.getUinput());
    }
    public void sendEvent(GamepadNative gamepad, int type, int button, int value) {
        sendEvent(gamepad.getUinput(), type, button, value);
    }

    private native void destroyVirtualGamepad(long evdev, long uinput);
    private native void sendEvent(long uinput, int type, int button, int value);

}