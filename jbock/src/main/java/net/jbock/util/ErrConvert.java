package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * Represents a runtime error during a converter invocation.
 */
public final class ErrConvert extends ParsingFailed {

    private final String converterFailure;
    private final Item item;

    /**
     * Public constructor.
     *
     * @param commandModel the command model
     * @param converterFailure an object describing the specific converter failure
     * @param item the item that the converter was bound to
     */
    public ErrConvert(
            CommandModel commandModel,
            String converterFailure,
            Item item) {
        super(commandModel);
        this.converterFailure = converterFailure;
        this.item = item;
    }

    /**
     * Returns the item that the converter was bound to.
     *
     * @return the item name
     */
    public Item item() {
        return item;
    }

    /**
     * Returns the error message that the converter returned.
     *
     * @return error message
     */
    public String converterFailure() {
        return converterFailure;
    }

    @Override
    public String message() {
        return "while converting " + item.namesOverviewError() + ": "
                + converterFailure;
    }
}
