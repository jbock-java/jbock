package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

public final class Util {

  public static String addBreaks(String code) {
    return code.replace(" ", "$W");
  }

  public static String typeToString(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return type.toString();
    }
    DeclaredType declared = TypeTool.AS_DECLARED.visit(type);
    String base = TypeTool.AS_TYPE_ELEMENT.visit(declared.asElement()).getSimpleName().toString();
    if (declared.getTypeArguments().isEmpty()) {
      return base;
    }
    return base + declared.getTypeArguments().stream().map(Util::typeToString)
        .collect(Collectors.joining(", ", "<", ">"));
  }
}
