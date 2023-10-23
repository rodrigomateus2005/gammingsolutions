package br.com.gammingsolution.model;

import lombok.Builder;
import lombok.Data;
import org.usb4java.LibUsb;

import java.util.List;

@Data
@Builder
public class UsbDevice {

    private short idVendor;
    private short idProduct;
    private String manufacturer;
    private String product;
    private String serialNumber;
    private int bus;
    private int port;
    private List<Byte> ports;

}

