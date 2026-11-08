package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * This failure object indicates that a required item
 * was not found among the command line parameters.
 */
public record ErrMissingItem(
        CommandModel commandModel,
        Item item) implements ParsingFailed {

    @Override
    public String message() {
        return "Missing required " + item.namesOverviewError();
    }
}
