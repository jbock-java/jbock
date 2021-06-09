package net.jbock.util;

import net.jbock.model.CommandModel;

public final class ExConvert extends Exception {

  private final Misconvert failure;

  private final ItemType itemType;

  private final String itemName;

  public ExConvert(Misconvert failure, ItemType itemType, String itemName) {
    this.failure = failure;
    this.itemType = itemType;
    this.itemName = itemName;
  }

  public NotSuccess toConverterError(CommandModel model) {
    return new ConverterError(model, failure, itemType, itemName);
  }
}
