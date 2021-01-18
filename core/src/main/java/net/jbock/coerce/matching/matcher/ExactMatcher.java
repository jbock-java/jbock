package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<UnwrapSuccess> tryUnwrapReturnType() {
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    return Optional.of(UnwrapSuccess.create(boxedReturnType(), constructorParam));
  }

  @Override
  public NonFlagSkew skew() {
    return NonFlagSkew.REQUIRED;
  }

  @Override
  public CodeBlock tailExpr() {
    return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", optionType(),
        enumName().enumConstant());
  }
}
