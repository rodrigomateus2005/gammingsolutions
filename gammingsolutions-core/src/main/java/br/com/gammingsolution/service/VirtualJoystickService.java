package br.com.gammingsolution.service;

import br.com.gammingsolution.virtualgamepad.VirtualGamepadBridge;
import br.com.gammingsolution.virtualgamepad.model.GamepadNative;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VirtualJoystickService {

    private static final int EV_KEY = 0x01;
    private static final int EV_ABS = 0x03;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VirtualGamepadBridge bridge;
    private final List<GamepadNative> gamepads;
    private final Map<Integer, Map<Integer, Integer>> map;

    public VirtualJoystickService() {
        bridge = new VirtualGamepadBridge();
        gamepads = new ArrayList<>();

        Map<Integer, Map<Integer, Integer>> map;
        try {
            InputStream in = VirtualJoystickService.class.getClassLoader().getResourceAsStream("joystick_map.json");

            map = objectMapper.readValue(in, Map.class);
        } catch (Exception e) {
            map = null;
        }
        this.map = map;
    }

    public long createNewJoystick() {
        var gamepad = bridge.createVirtualGamepad();
        gamepads.add(gamepad);
        return gamepads.indexOf(gamepad);
    }

    public void sendEvent(long joystick, int type, int keyCode, double value) {
        Double nativeValue = value * 12372;
        int nativeType = type == 0 ? EV_KEY : EV_ABS;
        int nativeKeyCode = map.get(type).get(keyCode);

        bridge.sendEvent(gamepads.get((int) joystick), nativeType, nativeKeyCode, nativeValue.intValue());
    }

    public void disconectJoystick(long joystick) {
        bridge.destroyVirtualGamepad(gamepads.get((int) joystick));
    }

}
