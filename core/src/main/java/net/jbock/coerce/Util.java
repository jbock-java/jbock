package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;

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

  public static CodeBlock arraysOfStringInvocation(List<String> strings) {
    Object[] args = new Object[1 + strings.size()];
    args[0] = Arrays.class;
    for (int i = 0; i < strings.size(); i++) {
      args[i + 1] = strings.get(i);
    }
    return CodeBlock.of(String.format("$T.asList($Z%s)",
        String.join(",$W", nCopies(strings.size(), "$S"))), args);
  }
}
