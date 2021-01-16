package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  abstract Optional<UnwrapSuccess> tryUnwrapReturnType();

  abstract NonFlagSkew skew();

  abstract CodeBlock tail();
}
