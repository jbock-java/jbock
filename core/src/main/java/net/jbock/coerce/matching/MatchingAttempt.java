package net.jbock.coerce.matching;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.EnumName;

import java.util.stream.Collectors;

class MatchingAttempt {

  // TODO this code belong in matchers
  static CodeBlock autoCollectExpr(ClassName optionType, EnumName enumName, NonFlagSkew skew) {
    switch (skew) {
      case OPTIONAL:
        return CodeBlock.of(".findAny()");
      case REQUIRED:
        return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", optionType,
            enumName.enumConstant());
      case REPEATABLE:
        return CodeBlock.of(".collect($T.toList())", Collectors.class);
      default:
        throw new AssertionError("unknown skew: " + skew);
    }
  }
}
