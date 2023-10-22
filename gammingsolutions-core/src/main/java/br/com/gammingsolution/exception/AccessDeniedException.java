package br.com.gammingsolution.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("Unable to open USB device: Access denied (insufficient permissions)");
    }

}
