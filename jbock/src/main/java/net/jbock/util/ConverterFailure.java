package net.jbock.util;

/**
 * Non-exceptional failure object that represents any converter failure.
 *
 * <p>This class is internal API and should not be used
 * in client code.
 */
public abstract class ConverterFailure {

    ConverterFailure() {
    }

    /**
     * Returns an error message to describe the failure.
     *
     * @return error message
     */
    abstract String converterMessage();
}
