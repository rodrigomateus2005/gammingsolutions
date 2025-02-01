package br.com.gammingsolution.virtualgamepad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualGamepadBridgeTest {

    @Test
    public void shouldCreateJoypadAndSendKey() {
        VirtualGamepadBridge bridge = new VirtualGamepadBridge();

        var gamepad = bridge.createVirtualGamepad();

        bridge.sendEvent(gamepad, 0x01, 0x130, 1);
        bridge.sendEvent(gamepad, 0x01, 0x130, 0);

        bridge.destroyVirtualGamepad(gamepad);
    }

}