package net.jbock.util;

import net.jbock.model.CommandModel;

import java.util.Locale;

/**
 * A wrapper to decorate {@link ConverterFailure} with some
 * additional information.
 */
public final class ConverterError extends ParsingError {

  private final ConverterFailure failure;
  private final ItemType itemType;
  private final String itemName;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param commandModel the command model
   * @param failure the converter failure
   * @param itemType type of the {@link ItemType item} that the converter was bound to
   * @param itemName item name
   */
  public ConverterError(
      CommandModel commandModel,
      ConverterFailure failure,
      ItemType itemType,
      String itemName) {
    super(commandModel);
    this.failure = failure;
    this.itemType = itemType;
    this.itemName = itemName;
  }

  /**
   * Returns the failure object.
   *
   * @return the failure object
   */
  public ConverterFailure failure() {
    return failure;
  }

  /**
   * Returns the name of the option or parameter that the converter was bound to.
   *
   * @return the item name
   */
  public String itemName() {
    return itemName;
  }

  /**
   * Returns the type of the option or parameter that the converter was bound to.
   *
   * @return the item type
   */
  public ItemType itemType() {
    return itemType;
  }

  @Override
  public String message() {
    return "while converting " + itemType.name().toLowerCase(Locale.US) +
        " " + itemName + ": " + failure.message();
  }
}
