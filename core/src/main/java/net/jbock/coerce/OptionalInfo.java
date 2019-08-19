package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

final class OptionalInfo {

  // Returns empty unless the return type is of the form Optional<?>.
  static Optional<TypeMirror> findOptionalInfo(BasicInfo basicInfo) {
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(basicInfo);
    if (optionalInfo.isPresent() && !basicInfo.optional) {
      throw basicInfo.asValidationException("Declare this parameter optional.");
    }
    if (!optionalInfo.isPresent() && basicInfo.optional) {
      throw basicInfo.asValidationException("Wrap the parameter type in Optional.");
    }
    return optionalInfo;
  }

  private static Optional<TypeMirror> findOptionalInfoInternal(BasicInfo basicInfo) {
    TypeMirror returnType = basicInfo.returnType();
    if (!basicInfo.tool().isSameErasure(returnType, Optional.class)) {
      return Optional.empty();
    }
    List<? extends TypeMirror> typeArgs = basicInfo.tool().typeargs(returnType);
    if (typeArgs.isEmpty()) {
      throw basicInfo.asValidationException("Add a type parameter");
    }
    return Optional.of(typeArgs.get(0));
  }
}
