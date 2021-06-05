package net.jbock.context;

import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import java.util.List;

public final class AllItems {

  private final List<Mapped<? extends AbstractItem>> parameters;
  private final boolean anyRequired;
  private final boolean anyDescriptionKeys;

  private AllItems(
      List<Mapped<? extends AbstractItem>> parameters,
      boolean anyRequired,
      boolean anyDescriptionKeys) {
    this.parameters = parameters;
    this.anyRequired = anyRequired;
    this.anyDescriptionKeys = anyDescriptionKeys;
  }

  static AllItems create(
      List<Mapped<PositionalParameter>> positionalParams,
      List<Mapped<NamedOption>> namedOptions,
      Util util) {
    List<Mapped<? extends AbstractItem>> items =
        util.concat(namedOptions, positionalParams);
    boolean anyRequired = items.stream().anyMatch(Mapped::isRequired);
    boolean anyDescriptionKeys = items.stream().anyMatch(c -> c.item().descriptionKey().isPresent());
    return new AllItems(items, anyRequired, anyDescriptionKeys);
  }

  public List<Mapped<? extends AbstractItem>> parameters() {
    return parameters;
  }

  public boolean anyRequired() {
    return anyRequired;
  }

  public boolean anyDescriptionKeys() {
    return anyDescriptionKeys;
  }
}
