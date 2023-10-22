package br.com.gammingsolution.audio;

import java.io.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class PipewireAudioBridge {

    public static final AudioFormat AUDIO_FORMAT = new AudioFormat(48000, 16, 2, true, false);

    public AudioFormat getAudioFormat() {
        return AUDIO_FORMAT;
    }

    static {
        // System.loadLibrary("pipewire_bridge"); // pipewire_bridge.dll (Windows) or libpipewire_bridge.so (Unixes)
        loadJarLibrary("libpipewire_bridge.so");
    }

    private static void loadJarLibrary(String name) {
        try {
            InputStream in = PipewireAudioBridge.class.getClassLoader().getResourceAsStream(name);
            byte[] buffer = new byte[1024];
            int read = -1;
            File temp = null;

            temp = File.createTempFile(name, "");
            FileOutputStream fos = new FileOutputStream(temp);

            while((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();

            System.load(temp.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private native void connect(ISoundListner listner);
    public native void stop();

    public Thread receiveAudio(ISoundListner listner) {
        var t = new Thread() {
            @Override
            public void run() {
                connect(listner);
            }
        };
        t.start();
        return t;
    }

}