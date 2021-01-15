package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  Optional<UnwrapSuccess> tryUnwrapReturnType() {
    return Optional.of(new UnwrapSuccess(boxedReturnType(), returnType(), constructorParam -> CodeBlock.of("$N", constructorParam)));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.REQUIRED;
  }

  @Override
  CodeBlock autoCollectExpr() {
    return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", optionType(),
        enumName().enumConstant());
  }
}
