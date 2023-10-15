package br.com.gammingsolution.model;

import lombok.Builder;
import lombok.Data;
import org.usb4java.LibUsb;

@Data
@Builder
public class UsbDevice {

    private short idVendor;
    private short idProduct;
    private String manufacturer;
    private String product;
    private String serialNumber;

}
