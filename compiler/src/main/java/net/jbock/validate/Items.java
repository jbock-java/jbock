package net.jbock.validate;

import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import java.util.List;

class Items {

  private final List<Mapped<PositionalParameter>> positionalParams;
  private final List<Mapped<NamedOption>> namedOptions;

  Items(
      List<Mapped<PositionalParameter>> positionalParams,
      List<Mapped<NamedOption>> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }

  List<Mapped<PositionalParameter>> positionalParams() {
    return positionalParams;
  }

  List<Mapped<NamedOption>> namedOptions() {
    return namedOptions;
  }
}
