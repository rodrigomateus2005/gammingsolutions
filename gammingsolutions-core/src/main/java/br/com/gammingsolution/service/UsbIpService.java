package br.com.gammingsolution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;

@Service
@Slf4j
public class UsbIpService {

    @Value("${command.attach}")
    private String commandAttach;

    public void registerClientModules() {
        try {
            var process = Runtime.getRuntime().exec("modprobe usbip_core");
            process = Runtime.getRuntime().exec("modprobe usbip_host");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void startDaemon() {
        try {
            var process = Runtime.getRuntime().exec("usbipd");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean bindDevice(String dev) {
        try {
            log.info("binding dev " + dev);
            var process = Runtime.getRuntime().exec("usbip bind -b " + dev);
            int ret = process.waitFor();
            return ret == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean attachDevice(String password, String ip, String dev) {
        try {
            var process = Runtime.getRuntime().exec("sudo " + commandAttach + " " + ip + " " + dev);
            int ret = process.waitFor();
            return ret == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
