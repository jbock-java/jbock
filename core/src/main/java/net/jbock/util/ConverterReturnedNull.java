package net.jbock.util;

/**
 * Indicates that a converter returned {@code null}.
 */
final class ConverterReturnedNull extends ConverterFailure {

    ConverterReturnedNull() {
    }

    @Override
    String converterMessage() {
        return "converter returned null";
    }
}
