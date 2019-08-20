package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

final class OptionalInfo {

  // Returns empty unless the return type is of the form Optional<?>.
  static TypeMirror findOptionalInfo(
      TypeTool tool,
      LiftedType liftedType,
      ExecutableElement sourceMethod) {
    if (!tool.isSameErasure(liftedType.liftedType(), Optional.class)) {
      throw new AssertionError(); // TODO catch this earlier
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(liftedType.liftedType());
    if (typeArgs.isEmpty()) {
      throw ValidationException.create(sourceMethod, "Add a type parameter");
    }
    return typeArgs.get(0);
  }
}
