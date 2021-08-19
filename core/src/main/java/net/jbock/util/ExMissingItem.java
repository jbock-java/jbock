package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * A checked exception to signal that a required item was
 * missing in the command line input.
 *
 * <p>This class is internal API and should not be used
 * in client code. It may be removed without warning in future
 * releases.
 */
public final class ExMissingItem extends ExNotSuccess {

    private final ItemType itemType;
    private final int itemIndex;

    /**
     * Public constructor.
     *
     * @param itemType the item type
     * @param itemIndex the item index
     */
    public ExMissingItem(ItemType itemType, int itemIndex) {
        this.itemType = itemType;
        this.itemIndex = itemIndex;
    }

    @Override
    public ParsingFailed toError(CommandModel model) {
        return new ErrMissingItem(model, model.getItem(itemType, itemIndex));
    }
}
