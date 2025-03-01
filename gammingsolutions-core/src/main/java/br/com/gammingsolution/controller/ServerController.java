package br.com.gammingsolution.controller;

import br.com.gammingsolution.audio.ISoundListner;
import br.com.gammingsolution.model.JoypadCommand;
import br.com.gammingsolution.service.IAudioService;
import br.com.gammingsolution.service.VNCService;
import br.com.gammingsolution.service.VirtualJoystickService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ServerController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IAudioService audioService;

    private final VirtualJoystickService joystickService;

    private final VNCService vncService;

    private Socket clientJoystick;

    private Socket clientAudio;

    public void start() {
        try {
            var t = new Thread(() -> {
                Thread joystickThread = null;
                Thread audioThread = null;

                try(
                        ServerSocket serverSocket = new ServerSocket(9001)
                ) {
                    clientAudio = serverSocket.accept();
                    clientJoystick = serverSocket.accept();

                    joystickThread = listenUsbPlugOnClient();
                    audioThread = audioService.addListner(new ServerSoundListner(clientAudio.getOutputStream()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (joystickThread != null) {
                        if (joystickThread.isAlive()) {
                            joystickThread.interrupt();
                        }
                    }
                    if (audioThread != null) {
                        if (audioThread.isAlive()) {
                            audioThread.interrupt();
                        }
                    }
                }
            });
            t.start();

            log.info("Starting vnc server");
            vncService.startVnc();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Thread listenUsbPlugOnClient() {
        var t = new Thread(() -> {
            var joypad = 0L;
            try {
                joypad = joystickService.createNewJoystick();
                log.info("Created joypad {}", joypad);

                var out = clientJoystick.getOutputStream();

                out.write(String.valueOf(joypad).getBytes());

                String command;
                var reader = new BufferedReader(new InputStreamReader(clientJoystick.getInputStream()));
                while ((command = reader.readLine()) != null) {
                    log.info("received joypad event: {}", command);

                    JoypadCommand joypadCommand = objectMapper.readValue(command, JoypadCommand.class);
                    joystickService.sendEvent(0, joypadCommand.getType(), joypadCommand.getKey(), joypadCommand.getValue());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                joystickService.disconectJoystick(joypad);
            }
        });
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
