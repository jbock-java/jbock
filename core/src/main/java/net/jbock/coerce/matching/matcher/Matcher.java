package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  public Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Optional<UnwrapSuccess> tryUnwrapReturnType();

  public abstract NonFlagSkew skew();

  public abstract CodeBlock tailExpr();
}
