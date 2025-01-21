package br.com.gammingsolution.virtualgamepad.model;

public class Gamepad {

    private final GamepadNative gamepadNative;

    Gamepad(GamepadNative gamepadNative) {
        this.gamepadNative = gamepadNative;
    }

    GamepadNative getNativeGamepad() {
        return this.gamepadNative;
    }

}
