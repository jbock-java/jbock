package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * Indicates that a required option or positional parameter
 * was missing on the command line.
 */
public final class ErrMissingItem extends ParsingFailed {

    private final Item item;

    /**
     * Public constructor.
     *
     * @param commandModel command model
     * @param item item
     */
    public ErrMissingItem(CommandModel commandModel, Item item) {
        super(commandModel);
        this.item = item;
    }

    /**
     * Returns the missing item.
     *
     * @return the item name
     */
    public Item item() {
        return item;
    }

    @Override
    public String message() {
        return "Missing required " + item.namesOverviewError();
    }
}
