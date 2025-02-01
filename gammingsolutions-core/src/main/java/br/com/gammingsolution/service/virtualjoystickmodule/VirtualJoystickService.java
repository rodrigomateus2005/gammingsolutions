package br.com.gammingsolution.service.virtualjoystickmodule;

import br.com.gammingsolution.service.IJoystickService;
import br.com.gammingsolution.virtualgamepad.VirtualGamepadBridge;
import br.com.gammingsolution.virtualgamepad.model.GamepadNative;

import java.util.ArrayList;
import java.util.List;

public class VirtualJoystickService implements IJoystickService {

    private VirtualGamepadBridge bridge;
    private List<GamepadNative> gamepads;

    public VirtualJoystickService() {
        bridge = new VirtualGamepadBridge();
        gamepads = new ArrayList<>();
    }

    @Override
    public long createNewJoystick() {
        var gamepad = bridge.createVirtualGamepad();
        gamepads.add(gamepad);
        return gamepads.indexOf(gamepad);
    }

    @Override
    public void sendEvent(long joystick, int type, int keyCode, int value) {
        bridge.sendEvent(gamepads.get((int) joystick), type, keyCode, value);
    }

    @Override
    public void disconectJoystick(long joystick) {
        bridge.destroyVirtualGamepad(gamepads.get((int) joystick));
    }

}
