package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * Non-exceptional failure object that represents
 * any runtime error during a converter invocation.
 */
public final class ErrConvert extends ParsingFailed {

    private final ConverterFailure converterFailure;
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
            ConverterFailure converterFailure,
            Item item) {
        super(commandModel);
        this.converterFailure = converterFailure;
        this.item = item;
    }

    /**
     * Returns the failure object.
     *
     * @return the failure object
     */
    public ConverterFailure failure() {
        return converterFailure;
    }

    /**
     * Returns the item that the converter was bound to.
     *
     * @return the item name
     */
    public Item item() {
        return item;
    }

    @Override
    public String message() {
        return "while converting " + item.namesOverviewError() + ": "
                + converterFailure.converterMessage();
    }
}
