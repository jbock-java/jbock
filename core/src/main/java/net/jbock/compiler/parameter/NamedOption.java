package net.jbock.compiler.parameter;

import net.jbock.coerce.Coercion;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.OptionalInt;

public class NamedOption extends Parameter {

  private final String optionName;

  private final char mnemonic;

  private final List<String> dashedNames;

  public NamedOption(char mnemonic, String optionName, ExecutableElement sourceMethod, String bundleKey, String sample, List<String> dashedNames, Coercion coercion, List<String> description) {
    super(sourceMethod, bundleKey, sample, coercion, description);
    this.dashedNames = dashedNames;
    this.mnemonic = mnemonic;
    this.optionName = optionName;
  }

  @Override
  public boolean isPositional() {
    return false;
  }

  @Override
  public OptionalInt positionalIndex() {
    return OptionalInt.empty();
  }

  @Override
  public OptionalInt positionalOrder() {
    return OptionalInt.empty();
  }

  @Override
  public List<String> dashedNames() {
    return dashedNames;
  }

  @Override
  public String optionName() {
    return optionName;
  }

  @Override
  public char mnemonic() {
    return mnemonic;
  }
}
