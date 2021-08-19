package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * Non-exceptional failure object which indicates that a required item
 * was missing on the command line.
 *
 * @see net.jbock.model.Multiplicity#REQUIRED
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
