package br.com.gammingsolution.controller;

import br.com.gammingsolution.audio.ISoundListner;
import br.com.gammingsolution.service.IAudioService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;

@Controller
public class ServerController {

    @Autowired
    private IAudioService audioService;

    private Thread audioThread;

    public void start() {
        try (ServerSocket audioServerSocket = new ServerSocket(9001)) {
            audioThread = audioService.addListner(new ServerSoundListner(audioServerSocket.accept().getOutputStream()));

            audioThread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
