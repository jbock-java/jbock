package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

public class OptionalWarning extends Warning {

  private static final Set<String> NAMES = Stream.of(
      Optional.class,
      OptionalInt.class,
      OptionalLong.class,
      OptionalDouble.class)
      .map(Class::getCanonicalName)
      .collect(Collectors.toSet());

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    for (TypeElement mirror : getTypeTree(type)) {
      String qname = mirror.getQualifiedName().toString();
      if (NAMES.contains(qname)) {
        return "Declare this parameter optional.";
      }
    }
    return null;
  }
}
