package br.com.gammingsolution.service;

import br.com.gammingsolution.model.UsbDevice;

import java.util.List;

public interface IUsbService {

    List<UsbDevice> listDevices();
    void registerHotPlug();

}
