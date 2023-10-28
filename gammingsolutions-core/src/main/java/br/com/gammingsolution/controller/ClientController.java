package br.com.gammingsolution.controller;

import br.com.gammingsolution.model.UsbDevice;
import br.com.gammingsolution.service.IAudioService;
import br.com.gammingsolution.service.IUsbService;
import br.com.gammingsolution.service.UsbIpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ClientController {

    @Value("${joysticks}")
    private List<String> joysticks;

    @Autowired
    private IAudioService audioService;

    @Autowired
    private IUsbService usbService;

    @Autowired
    private UsbIpService usbIpService;

    private Thread audioThread;

    public void connect(String host) {
        log.info("Connecting to " + host);
        try {
            usbIpService.startDaemon();

            Socket clientSocket = new Socket(host, 9001);

            usbService.registerHotPlug((UsbDevice device) -> {
                try {
                    if (isDeviceInList(device)) {
                        String budId = getBusId(device);
                        usbIpService.bindDevice(budId);

                        var out = new PrintWriter(clientSocket.getOutputStream());
                        out.println(budId);
                        out.flush();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });

            audioThread = connectAudio(clientSocket.getInputStream());
            audioThread.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDeviceInList(UsbDevice device) {
        String idInternal = device.getIdVendor() + "-" + device.getIdProduct();

        return joysticks
                .stream()
                .anyMatch(x -> StringUtils.equals(x, idInternal));
    }

    private String getBusId(UsbDevice device) {
        return device.getBus() + "-" + StringUtils.join(device.getPorts(), ".");
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
