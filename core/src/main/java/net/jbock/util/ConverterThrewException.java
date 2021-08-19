package net.jbock.util;

/**
 * Indicates that an exception was thrown from a converter.
 */
final class ConverterThrewException extends ConverterFailure {

    private final Exception exception;

    ConverterThrewException(Exception exception) {
        this.exception = exception;
    }

    @Override
    String converterMessage() {
        return exception.getMessage();
    }
}
