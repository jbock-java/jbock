package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class OptionalInfo {

  public final boolean optional;

  public final TypeMirror baseType;

  private OptionalInfo(boolean optional, TypeMirror baseType) {
    this.optional = optional;
    this.baseType = baseType;
  }

  static OptionalInfo simple(TypeMirror baseType) {
    return new OptionalInfo(false, baseType);
  }

  private static OptionalInfo optional(TypeMirror baseType) {
    return new OptionalInfo(true, baseType);
  }

  public static OptionalInfo findOptionalInfo(TypeMirror mirror, boolean optional) {
    if (!optional) {
      return OptionalInfo.simple(mirror);
    }
    TypeTool tool = TypeTool.get();
    if (!tool.eql(tool.erasure(mirror), tool.declared(Optional.class))) {
      return OptionalInfo.simple(mirror);
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(mirror);
    if (typeArgs.isEmpty()) {
      return OptionalInfo.simple(mirror);
    }
    return OptionalInfo.optional(typeArgs.get(0));
  }

}
