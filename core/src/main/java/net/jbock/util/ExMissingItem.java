package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Internal exception that may be thrown and caught
 * in the generated code.
 */
public final class ExMissingItem extends ExNotSuccess {

    private final ItemType itemType;
    private final int itemIndex;

    /**
     * Public constructor that may be invoked from the generated code.
     *
     * @param itemType the item type
     * @param itemIndex the item index
     */
    public ExMissingItem(ItemType itemType, int itemIndex) {
        this.itemType = itemType;
        this.itemIndex = itemIndex;
    }

    @Override
    public NotSuccess toError(CommandModel model) {
        return new ErrMissingItem(model, model.getItem(itemType, itemIndex));
    }
}
