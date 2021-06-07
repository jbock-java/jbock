package net.jbock.util;

import net.jbock.model.CommandModel;

import java.util.Locale;

/**
 * An instance of this class signals failure of a converter invocation.
 */
public final class ConverterError extends NotSuccess implements HasMessage {

  private final Misconvert misconvert;
  private final ItemType itemType;
  private final String itemName;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param commandModel the command model
   * @param misconvert an object describing the specific converter failure
   * @param itemType type of the {@link ItemType item} that the converter was bound to
   * @param itemName item name
   */
  public ConverterError(
      CommandModel commandModel,
      Misconvert misconvert,
      ItemType itemType,
      String itemName) {
    super(commandModel);
    this.misconvert = misconvert;
    this.itemType = itemType;
    this.itemName = itemName;
  }

  /**
   * Returns the failure object.
   *
   * @return the failure object
   */
  public Misconvert misconvert() {
    return misconvert;
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
        " " + itemName + ": " + misconvert.converterMessage();
  }
}
