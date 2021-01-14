package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.function.Function;

class ExactMatcher extends Matcher {

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
