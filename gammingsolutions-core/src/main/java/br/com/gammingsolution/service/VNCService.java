package br.com.gammingsolution.service;

import org.springframework.stereotype.Service;

@Service
public class VNCService {

    public void startVnc() {
        try {
            var process = Runtime.getRuntime().exec("x0vncserver -fg -localhost no -AcceptSetDesktopSize 0 -display :2");

            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
