package net.jbock.util;

/**
 * Indicates that an exception was thrown from a converter.
 */
public final class ConverterThrewException extends ConverterFailure {

    private final Exception exception;

    ConverterThrewException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Returns the exception that was thrown from the converter.
     *
     * @return the exception
     */
    public Exception exception() {
        return exception;
    }

    @Override
    String converterMessage() {
        return exception.getMessage();
    }
}
