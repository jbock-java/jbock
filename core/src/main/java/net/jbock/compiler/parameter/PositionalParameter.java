package net.jbock.compiler.parameter;

import net.jbock.coerce.Coercion;

import javax.lang.model.element.ExecutableElement;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

public class PositionalParameter extends Parameter {

  private final int positionalIndex;

  public PositionalParameter(ExecutableElement sourceMethod, String bundleKey, String sample, List<String> dashedNames, Coercion coercion, List<String> description, int positionalIndex) {
    super(sourceMethod, bundleKey, sample, coercion, description);
    this.positionalIndex = positionalIndex;
  }

  @Override
  public boolean isPositional() {
    return true;
  }

  @Override
  public OptionalInt positionalIndex() {
    return OptionalInt.of(positionalIndex);
  }

  @Override
  public OptionalInt positionalOrder() {
    return OptionalInt.of(isRepeatable() ? 2 : isOptional() ? 1 : 0);
  }

  @Override
  public List<String> dashedNames() {
    return Collections.emptyList();
  }

  @Override
  public String optionName() {
    return null;
  }

  @Override
  public char mnemonic() {
    return ' ';
  }
}
