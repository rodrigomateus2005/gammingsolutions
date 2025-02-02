package br.com.gammingsolution.service;

import br.com.gammingsolution.virtualgamepad.VirtualGamepadBridge;
import br.com.gammingsolution.virtualgamepad.model.GamepadNative;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VirtualJoystickService {

    private final VirtualGamepadBridge bridge;
    private final List<GamepadNative> gamepads;

    public VirtualJoystickService() {
        bridge = new VirtualGamepadBridge();
        gamepads = new ArrayList<>();
    }

    public long createNewJoystick() {
        var gamepad = bridge.createVirtualGamepad();
        gamepads.add(gamepad);
        return gamepads.indexOf(gamepad);
    }

    public void sendEvent(long joystick, int type, int keyCode, int value) {
        bridge.sendEvent(gamepads.get((int) joystick), type, keyCode, value);
    }

    public void disconectJoystick(long joystick) {
        bridge.destroyVirtualGamepad(gamepads.get((int) joystick));
    }

}
