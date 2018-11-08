package net.jbock.coerce.hint;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

public class OptionalPrimitiveHint extends Hint {

  private static final Set<String> NAMES = Stream.of(
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
        if (qname.contains("OptionalInt")) {
          return "Use Optional<Integer>.";
        }
        if (qname.contains("OptionalLong")) {
          return "Use Optional<Long>.";
        }
        if (qname.contains("OptionalDouble")) {
          return "Use Optional<Double>.";
        }
      }
    }
    return null;
  }
}
