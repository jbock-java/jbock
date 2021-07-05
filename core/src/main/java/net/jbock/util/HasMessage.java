package net.jbock.util;

/**
 * Superinterface of all non-exceptional failure objects which have
 * an error message attached to them.
 */
public interface HasMessage {

    /**
     * Returns an error message that describes the error.
     *
     * @return the error message
     */
    String message();
}
