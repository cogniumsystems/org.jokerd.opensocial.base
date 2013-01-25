package org.jokerd.opensocial.cursors;

import java.io.IOException;

/**
 * @author kotelnikov
 */
public class StreamException extends IOException {

    private static final long serialVersionUID = 6502717837737775759L;

    public StreamException() {
        super();
    }

    public StreamException(String message) {
        super(message);
    }

    public StreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamException(Throwable cause) {
        super(cause);
    }

}