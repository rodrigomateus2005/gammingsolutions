package br.com.gammingsolution.controller;

import br.com.gammingsolution.audio.ISoundListner;
import br.com.gammingsolution.service.IAudioService;
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
    private UsbIpService usbIpService;

    private Socket client;
    private Thread audioThread;
    private Thread joystickThread;

    private String password;

    public void start() {
        try (
                ServerSocket serverSocket = new ServerSocket(9001);
        ) {
            client = serverSocket.accept();

            joystickThread = listenUsbPlugOnClient();

            audioThread = audioService.addListner(new ServerSoundListner(client.getOutputStream()));
            audioThread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Thread listenUsbPlugOnClient() {
        var t = new Thread() {
            @Override
            public void run() {
                try {
                    String resp;
                    var reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    while ((resp = reader.readLine()) != null) {
                        var busId = resp;
                        var ip = client.getInetAddress().getHostName();

                        log.info("Attaching device " + busId + " on client " + ip);

                        usbIpService.attachDevice(ip, busId);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
