package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import java.util.Optional;

public class OptionalMatcher extends Matcher {

  private final Optionalish optionalish;

  @Inject
  OptionalMatcher(ParameterContext parameterContext, Optionalish optionalish) {
    super(parameterContext);
    this.optionalish = optionalish;
  }

  @Override
  Optional<UnwrapSuccess> tryUnwrapReturnType() {
    return optionalish.unwrap(returnType());
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.OPTIONAL;
  }

  @Override
  CodeBlock tailExpr() {
    return CodeBlock.of(".findAny()");
  }
}
