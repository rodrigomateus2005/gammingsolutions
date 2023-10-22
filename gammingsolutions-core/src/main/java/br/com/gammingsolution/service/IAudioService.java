package br.com.gammingsolution.service;

import br.com.gammingsolution.audio.ISoundListner;

import javax.sound.sampled.TargetDataLine;

public interface IAudioService {

    Thread addListner(ISoundListner listner);

    void playBytes(byte[] bytes);

}
