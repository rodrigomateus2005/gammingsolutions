package br.com.gammingsolution.controller;

import br.com.gammingsolution.service.IAudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.Socket;

@Controller
public class ClientController {

    @Autowired
    private IAudioService audioService;

    private Thread audioThread;

    public void connect() {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 9001);
            audioThread = connectAudio(clientSocket.getInputStream());

            audioThread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Thread connectAudio(InputStream in) {
        var t = new Thread() {
            @Override
            public void run() {
                try {
                    byte[] bytes;
                    while ((bytes = in.readNBytes(1024)) != null) {
                        audioService.playBytes(bytes);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
        return t;
    }

}
