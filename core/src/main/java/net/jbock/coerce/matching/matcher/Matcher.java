package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

public abstract class Matcher extends ParameterScoped {

  public Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Either<String, UnwrapSuccess> tryUnwrapReturnType();

  public abstract Skew skew();

  public abstract CodeBlock tailExpr();
}
