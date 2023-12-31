package br.com.gammingsolution.service.audiomodule;

import br.com.gammingsolution.audio.ISoundListner;
import br.com.gammingsolution.audio.PipewireAudioBridge;
import br.com.gammingsolution.service.IAudioService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;

@Service
public class AudioModuleService implements IAudioService {

    private PipewireAudioBridge bridge;
    private SourceDataLine speaker;

    public AudioModuleService() {
        bridge = new PipewireAudioBridge();
        loadSpeakers();
    }

    private void loadSpeakers() {
        AudioFormat audioFormat = bridge.getAudioFormat();
        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(audioFormat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Thread addListner(ISoundListner listner) {
        return bridge.receiveAudio(listner);
    }

    @Override
    public void playBytes(byte[] bytes) {
        try {
            if (Collections.max(Arrays.asList(ArrayUtils.toObject(bytes))) > 0)
                speaker.write(bytes, 0, bytes.length);
            if (!speaker.isRunning())
                speaker.start();
        } catch (Exception ex) {
            System.out.println("Error occured during playback process:" + ex.getMessage());
        }
    }
}
