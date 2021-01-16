package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
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
    ParameterSpec constructorParam = constructorParam(returnType());
    return Optional.of(UnwrapSuccess.create(boxedReturnType(), constructorParam));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.REQUIRED;
  }

  @Override
  CodeBlock tail() {
    return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", optionType(),
        enumName().enumConstant());
  }
}
