package br.com.gammingsolution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UsbIpService {

    public void registerServerModules() {
        try {
            var process = Runtime.getRuntime().exec("modprobe vhci-hcd");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

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

    public boolean attachDevice(String ip, String dev) {
        try {
            var process = Runtime.getRuntime().exec("usbip attach -r " + ip + " -b " + dev);
            int ret = process.waitFor();
            return ret == 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
