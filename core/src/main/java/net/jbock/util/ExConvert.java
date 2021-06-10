package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates that an error occurred within a particular converter.
 * Internal exception that may be thrown and caught
 * in the generated code.
 */
public final class ExConvert extends ExNotSuccess {

  private final ConverterFailure failure;
  private final ItemType itemType;
  private final int itemIndex;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param failure the failure
   * @param itemType the item type
   * @param itemIndex the item index
   */
  public ExConvert(ConverterFailure failure, ItemType itemType, int itemIndex) {
    this.failure = failure;
    this.itemType = itemType;
    this.itemIndex = itemIndex;
  }

  @Override
  public NotSuccess toError(CommandModel model) {
    return new ErrConvert(model, failure, model.getItem(itemType, itemIndex));
  }
}
