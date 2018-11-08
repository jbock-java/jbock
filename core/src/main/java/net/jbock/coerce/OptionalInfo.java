package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public final class OptionalInfo {

  // Returns empty unless the return type is of the form Optional<?>.
  // Note, it can be return emtpy while basicInfo.optional is true,
  // if the return type is e.g. OptionalInt.
  public static Optional<TypeMirror> findOptionalInfo(TypeMirror mirror, boolean optional) {
    if (!optional) {
      return Optional.empty();
    }
    TypeTool tool = TypeTool.get();
    if (!tool.isSameErasure(mirror, Optional.class)) {
      return Optional.empty();
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(mirror);
    if (typeArgs.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(typeArgs.get(0));
  }
}
