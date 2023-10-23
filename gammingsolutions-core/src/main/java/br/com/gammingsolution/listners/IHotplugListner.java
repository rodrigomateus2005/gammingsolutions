package br.com.gammingsolution.listners;

import br.com.gammingsolution.model.UsbDevice;

public interface IHotplugListner {

    void eventHandle(UsbDevice device);

}
