package dk.kb.webdanica.exceptions;

import dk.netarkivet.common.exceptions.NetarkivetException;

/**
 * 
 */
@SuppressWarnings({"serial"})
public class WebdanicaException extends NetarkivetException {

    /**
     * Create a new WebdanicaException.
     *
     * @param message Explanatory message
     */
    public WebdanicaException (String message) {
        super(message);
    }

    /**
     * Create a new WebdanicaException.
     *
     * @param message Explanatory message
     * @param cause The exception that prompted the exception
     */
    public WebdanicaException(String message, Throwable cause) {
        super(message, cause);
    }
}

