package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * A checked exception to signal that an error has occurred
 * within a particular converter.
 *
 * <p>This class is internal API and should not be used
 * in client code. It may be removed without warning in future
 * releases.
 */
public final class ExConvert extends ExNotSuccess {

    private final ConverterFailure failure;
    private final ItemType itemType;
    private final int itemIndex;

    /**
     * Public constructor.
     *
     * @param failure the specific failure that has occurred
     * @param itemType the item type
     * @param itemIndex the index of the converted item
     *                  within {@link CommandModel#parameters()} or
     *                  {@link CommandModel#options()}
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
