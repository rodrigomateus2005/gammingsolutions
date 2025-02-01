package br.com.gammingsolution.controller;

import br.com.gammingsolution.audio.ISoundListner;
import br.com.gammingsolution.service.IAudioService;
import br.com.gammingsolution.service.IJoystickService;
import br.com.gammingsolution.service.UsbIpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Controller
@Slf4j
public class ServerController {

    @Autowired
    private IAudioService audioService;

    @Autowired
    private IJoystickService joystickService;

    private Socket clientAudio;
    private Socket clientJoystick;
    private Thread audioThread;
    private Thread joystickThread;

    private String password;

    public void start() {
        try (
                ServerSocket serverSocket = new ServerSocket(9001);
        ) {
            clientAudio = serverSocket.accept();
            clientJoystick = serverSocket.accept();

            joystickThread = listenUsbPlugOnClient();
            joystickThread.join();

            audioThread = audioService.addListner(new ServerSoundListner(clientAudio.getOutputStream()));
            audioThread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Thread listenUsbPlugOnClient() {
        var t = new Thread() {
            @Override
            public void run() {
                var joypad = 0L;
                try {
                    joypad = joystickService.createNewJoystick();
                    log.info("Created joypad {}", joypad);

                    var out = clientJoystick.getOutputStream();

                    out.write(String.valueOf(joypad).getBytes());

                    String command;
                    var reader = new BufferedReader(new InputStreamReader(clientJoystick.getInputStream()));
                    while ((command = reader.readLine()) != null) {
                        // usbIpService.attachDevice(ip, busId);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    joystickService.disconectJoystick(joypad);
                }
            }
        };
        t.start();
        return t;
    }

    private record ServerSoundListner(OutputStream out) implements ISoundListner {
        @Override
        public void onProcess(int channels, byte[] bytes) {
            try {
                out.write(bytes);
            } catch (IOException ignored) {
            }
        }
    }

}
