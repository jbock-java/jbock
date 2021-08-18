package net.jbock.util;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.model.CommandModel;

/**
 * A checked exception to signal that an error has occurred
 * within a particular converter.
 *
 * @see Option#converter()
 * @see Parameter#converter()
 * @see Parameters#converter()
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
