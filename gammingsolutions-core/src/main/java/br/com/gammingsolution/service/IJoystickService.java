package br.com.gammingsolution.service;

public interface IJoystickService {

    long createNewJoystick();
    void sendEvent(long joystick, int type, int keyCode, int value);
    void disconectJoystick(long joystick);

}
