package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  public Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Either<String, UnwrapSuccess> tryUnwrapReturnType();

  public abstract NonFlagSkew skew();

  public abstract CodeBlock tailExpr();
}
