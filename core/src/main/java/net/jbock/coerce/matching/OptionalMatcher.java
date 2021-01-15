package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;

import javax.inject.Inject;
import java.util.Optional;

public class OptionalMatcher extends Matcher {

  @Inject
  OptionalMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  Optional<UnwrapSuccess> tryUnwrapReturnType() {
    return Optionalish.unwrap(returnType(), tool())
        .map(opt -> new UnwrapSuccess(opt.wrappedType(), opt.liftedType(), opt::extractExpr));
  }

  @Override
  NonFlagSkew skew() {
    return NonFlagSkew.OPTIONAL;
  }

  @Override
  CodeBlock autoCollectExpr() {
    return CodeBlock.of(".findAny()");
  }
}
