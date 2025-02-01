package br.com.gammingsolution.virtualgamepad.model;

public class GamepadNative {

    private long evdev = 0;

    public long getUinput() {
        return uinput;
    }

    public long getEvdev() {
        return evdev;
    }

    private long uinput = 0;

    public GamepadNative(long evdev, long uinput) {

    }
}