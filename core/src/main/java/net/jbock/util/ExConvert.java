package net.jbock.util;

import net.jbock.model.CommandModel;

public final class ExConvert extends Exception {

  private final Misconvert failure;

  private final ItemType itemType;

  private final int itemIndex;

  public ExConvert(Misconvert failure, ItemType itemType, int itemIndex) {
    this.failure = failure;
    this.itemType = itemType;
    this.itemIndex = itemIndex;
  }

  public NotSuccess toConverterError(CommandModel model) {
    switch (itemType) {
      case PARAMETER:
        return new ConverterError(model, failure, model.parameters().get(itemIndex));
      case OPTION:
        return new ConverterError(model, failure, model.options().get(itemIndex));
      default:
        throw new AssertionError("all cases exhausted");
    }
  }
}
