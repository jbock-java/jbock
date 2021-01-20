package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.ParameterContext;
import net.jbock.either.Either;

import javax.inject.Inject;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Either<String, UnwrapSuccess> tryUnwrapReturnType() {
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    return Either.right(UnwrapSuccess.create(boxedReturnType(), constructorParam, 0));
  }

  @Override
  public Skew skew() {
    return Skew.REQUIRED;
  }

  @Override
  public CodeBlock tailExpr() {
    return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", optionType(),
        enumName().enumConstant());
  }
}
