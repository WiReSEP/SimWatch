package de.tu_bs.wire.simwatch.api;


public class RegistrationException extends RuntimeException {
    public RegistrationException(Throwable cause) {
        super(cause);
    }

    public RegistrationException(String message) {
        super(message);
    }
}
