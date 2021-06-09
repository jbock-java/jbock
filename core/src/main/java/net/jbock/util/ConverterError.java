package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

import java.util.Locale;

/**
 * An instance of this class signals failure of a converter invocation.
 */
public final class ConverterError extends NotSuccess implements HasMessage {

  private final Misconvert misconvert;
  private final Item item;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param commandModel the command model
   * @param misconvert an object describing the specific converter failure
   * @param item the item that the converter was bound to
   */
  public ConverterError(
      CommandModel commandModel,
      Misconvert misconvert,
      Item item) {
    super(commandModel);
    this.misconvert = misconvert;
    this.item = item;
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
   * Returns the item that the converter was bound to.
   *
   * @return the item name
   */
  public Item item() {
    return item;
  }

  @Override
  public String message() {
    return "while converting " + item.itemType().name().toLowerCase(Locale.US) +
        " " + item.errorOverview() + ": " + misconvert.converterMessage();
  }
}
