package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

final class OptionalInfo {

  // Returns empty unless the return type is of the form Optional<?>.
  static Optional<TypeMirror> findOptionalInfo(
      TypeTool tool,
      boolean optional,
      LiftedType liftedType,
      ExecutableElement sourceMethod) {
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(tool, liftedType, sourceMethod);
    if (optionalInfo.isPresent() && !optional) {
      throw ValidationException.create(sourceMethod, "Declare this parameter optional.");
    }
    if (!optionalInfo.isPresent() && optional) {
      throw ValidationException.create(sourceMethod, "Wrap the parameter type in Optional.");
    }
    return optionalInfo;
  }

  private static Optional<TypeMirror> findOptionalInfoInternal(
      TypeTool tool,
      LiftedType liftedType,
      ExecutableElement sourceMethod) {
    TypeMirror returnType = liftedType.liftedType();
    if (!tool.isSameErasure(returnType, Optional.class)) {
      return Optional.empty();
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(returnType);
    if (typeArgs.isEmpty()) {
      throw ValidationException.create(sourceMethod, "Add a type parameter");
    }
    return Optional.of(typeArgs.get(0));
  }
}
