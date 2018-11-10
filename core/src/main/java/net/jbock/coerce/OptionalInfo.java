package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

final class OptionalInfo {

  // Returns empty unless the return type is of the form Optional<?>.
  static Optional<TypeMirror> findOptionalInfo(BasicInfo basicInfo) throws TmpException {
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(basicInfo);
    if (optionalInfo.isPresent() && !basicInfo.optional) {
      throw TmpException.create("Declare this parameter optional.");
    }
    if (!optionalInfo.isPresent() && basicInfo.optional) {
      throw TmpException.create("Wrap the parameter type in Optional.");
    }
    return optionalInfo;
  }

  private static Optional<TypeMirror> findOptionalInfoInternal(BasicInfo basicInfo) throws TmpException {
    TypeTool tool = TypeTool.get();
    TypeMirror returnType = basicInfo.returnType();
    if (!tool.isSameErasure(returnType, Optional.class)) {
      return Optional.empty();
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(returnType);
    if (typeArgs.isEmpty()) {
      throw TmpException.create("Add a type parameter");
    }
    return Optional.of(typeArgs.get(0));
  }
}
