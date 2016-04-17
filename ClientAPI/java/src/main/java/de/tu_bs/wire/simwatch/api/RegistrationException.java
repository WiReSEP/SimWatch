package de.tu_bs.wire.simwatch.api;


@SuppressWarnings("WeakerAccess") // thrown by public api, therefore must be public
public class RegistrationException extends Exception {
    RegistrationException(Throwable cause) {
        super(cause);
    }

    RegistrationException(String message) {
        super(message);
    }
}
