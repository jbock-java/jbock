package net.jbock.context;

import net.jbock.convert.Mapped;
import net.jbock.parameter.PositionalParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PositionalParameters {

  private final List<Mapped<PositionalParameter>> regular; // (optional|required)
  private final Optional<Mapped<PositionalParameter>> repeatable;

  private PositionalParameters(
      List<Mapped<PositionalParameter>> regular,
      Optional<Mapped<PositionalParameter>> repeatable) {
    this.regular = regular;
    this.repeatable = repeatable;
  }

  public static PositionalParameters create(List<Mapped<PositionalParameter>> all) {
    List<Mapped<PositionalParameter>> regular = all.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toUnmodifiableList());
    Optional<Mapped<PositionalParameter>> repeatable = all.stream()
        .filter(Mapped::isRepeatable)
        .findFirst();
    return new PositionalParameters(regular, repeatable);
  }

  public List<Mapped<PositionalParameter>> regular() {
    return regular;
  }

  public List<Mapped<PositionalParameter>> parameters() {
    if (repeatable.isEmpty()) {
      return regular;
    }
    List<Mapped<PositionalParameter>> result = new ArrayList<>(regular.size() + 1);
    result.addAll(regular);
    repeatable.ifPresent(result::add);
    return result;
  }

  public int size() {
    return regular().size() + (anyRepeatable() ? 1 : 0);
  }

  public Optional<Mapped<PositionalParameter>> repeatable() {
    return repeatable;
  }

  public boolean anyRepeatable() {
    return repeatable.isPresent();
  }

  public boolean isEmpty() {
    return regular.isEmpty() && !anyRepeatable();
  }
}
