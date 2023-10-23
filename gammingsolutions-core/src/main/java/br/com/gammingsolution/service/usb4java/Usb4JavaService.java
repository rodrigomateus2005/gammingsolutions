package br.com.gammingsolution.service.usb4java;

import br.com.gammingsolution.exception.AccessDeniedException;
import br.com.gammingsolution.listners.IHotplugListner;
import br.com.gammingsolution.model.UsbDevice;
import br.com.gammingsolution.service.IUsbService;
import lombok.extern.slf4j.Slf4j;
import org.jline.utils.Log;
import org.springframework.stereotype.Service;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class Usb4JavaService implements IUsbService, AutoCloseable {

    private static class EventHandlingThread extends Thread {
        private volatile boolean abort;

        public void abort() {
            this.abort = true;
        }

        @Override
        public void run() {
            while (!this.abort) {
                int result = LibUsb.handleEventsTimeout(null, 1000000);
                if (result != LibUsb.SUCCESS)
                    throw new LibUsbException("Unable to handle events", result);
            }
        }
    }

    private final EventHandlingThread eventHandlingThread;
    private final List<HotplugCallbackHandle> hotplugCallbackList = new ArrayList<>();

    private final Context context;

    public Usb4JavaService() {
        context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);

        eventHandlingThread = new EventHandlingThread();
        eventHandlingThread.start();
    }

    @Override
    public void close() throws Exception {
        eventHandlingThread.abort();
        for (var callback : hotplugCallbackList) {
            LibUsb.hotplugDeregisterCallback(context, callback);
        }
        eventHandlingThread.join();
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
            ByteBuffer path = BufferUtils.allocateByteBuffer(8);
            result = LibUsb.getPortNumbers(device, path);
            List<Byte> ports = new ArrayList<Byte>();

            if (result > 0)
            {
                for (int i = 0; i < result; i++)
                {
                    ports.add(path.get(i));
                }
            }

            return UsbDevice.builder()
                    .idProduct(descriptor.idProduct())
                    .idVendor(descriptor.idVendor())
                    .serialNumber(LibUsb.getStringDescriptor(handle, descriptor.iSerialNumber()))
                    .manufacturer(LibUsb.getStringDescriptor(handle, descriptor.iManufacturer()))
                    .product(LibUsb.getStringDescriptor(handle, descriptor.iProduct()))
                    .bus(LibUsb.getBusNumber(device))
                    .port(LibUsb.getPortNumber(device))
                    .ports(ports)
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
    public void registerHotPlug(IHotplugListner listner) {
        if (!LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)) {
            Log.error("libusb doesn't support hotplug on this system");
            return;
        }

        HotplugCallbackHandle callbackHandle = new HotplugCallbackHandle();
        var result = LibUsb.hotplugRegisterCallback(context,
                LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED
                        | LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                LibUsb.HOTPLUG_ENUMERATE,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                (context, device, i, o) -> {
                    try {
                        if (i == LibUsb.HOTPLUG_EVENT_DEVICE_LEFT) {
                            log.info("desconected " + device);
                        } else {
                            listner.eventHandle(mapDevice(device));
                        }
                    } catch (AccessDeniedException e) {
                    }
                    return 0;
                }, null, callbackHandle);


        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to register hotplug callback",
                    result);
        } else {
            this.hotplugCallbackList.add(callbackHandle);
        }
    }

    private Optional<UsbDevice> findDevice(short vendorId, short productId) {
        return this.listDevices()
                .stream()
                .filter(x -> x.getIdVendor() == vendorId && x.getIdProduct() == productId)
                .findAny();
    }

}
