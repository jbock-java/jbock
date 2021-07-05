package net.jbock.util;

/**
 * Non-exceptional failure object that represents any converter failure.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link ConverterThrewException}
 *   <li>{@link ConverterReturnedNull}
 * </ul>
 */
public abstract class ConverterFailure {

    ConverterFailure() {
    }

    /**
     * Returns an error message to describe the failure.
     *
     * @return error message
     */
    public abstract String converterMessage();
}
