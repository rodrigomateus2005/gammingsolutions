package br.com.gammingsolution.service.usb4java;

import br.com.gammingsolution.exception.AccessDeniedException;
import br.com.gammingsolution.model.UsbDevice;
import br.com.gammingsolution.service.IUsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.usb4java.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class Usb4JavaService implements IUsbService {

    private final Context context;

    public Usb4JavaService() {
        context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
    }

    private UsbDevice mapDevice(Device device) {
        DeviceDescriptor descriptor = new DeviceDescriptor();
        int result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);

        DeviceHandle handle = new DeviceHandle();
        result = LibUsb.open(device, handle);
        if (result == LibUsb.ERROR_ACCESS)
            throw new AccessDeniedException();
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException("Unable to open USB device", result);
        try {
            return UsbDevice.builder()
                    .idProduct(descriptor.idProduct())
                    .idVendor(descriptor.idVendor())
                    .serialNumber(LibUsb.getStringDescriptor(handle, descriptor.iSerialNumber()))
                    .manufacturer(LibUsb.getStringDescriptor(handle, descriptor.iManufacturer()))
                    .product(LibUsb.getStringDescriptor(handle, descriptor.iProduct()))
                    .build();
        } finally {
            LibUsb.close(handle);
        }
    }

    public List<UsbDevice> listDevices() {
        List<UsbDevice> usbDevices = new ArrayList<UsbDevice>();

        // Read the USB device list
        DeviceList list = new DeviceList();
        final int result = LibUsb.getDeviceList(context, list);
        if (result < LibUsb.SUCCESS) throw new LibUsbException("Unable to get device list", result);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                try {
                    usbDevices.add(mapDevice(device));
                } catch (AccessDeniedException e) {
                    log.debug(e.getMessage());
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        return usbDevices;
    }

    @Override
    public void registerHotPlug() {

    }

    private Optional<UsbDevice> findDevice(short vendorId, short productId) {
        return this.listDevices()
                .stream()
                .filter(x -> x.getIdVendor() == vendorId && x.getIdProduct() == productId)
                .findAny();
    }

}
