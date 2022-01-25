package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.ItemType;

/**
 * A checked exception to signal that an error has occurred
 * within a particular converter.
 *
 * <p>This class is internal API and should not be used
 * in client code. It may be removed without warning in future
 * releases.
 */
public final class ExConvert extends ExFailure {

    private final ConverterFailure failure;
    private final ItemType itemType;
    private final int itemIndex;

    /**
     * Public constructor.
     *
     * @param failure the specific failure that has occurred
     * @param itemType the item type (option or parameter)
     * @param itemIndex the index of the item (option or parameter)
     *                  within {@link CommandModel#options()} or
     *                  {@link CommandModel#parameters()}
     */
    public ExConvert(ConverterFailure failure, ItemType itemType, int itemIndex) {
        this.failure = failure;
        this.itemType = itemType;
        this.itemIndex = itemIndex;
    }

    @Override
    public ParsingFailed toError(CommandModel model) {
        return new ErrConvert(model, failure, model.getItem(itemType, itemIndex));
    }
}
