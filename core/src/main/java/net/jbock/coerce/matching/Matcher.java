package net.jbock.coerce.matching;

import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.lang.model.element.TypeElement;

abstract class Matcher extends ParameterScoped {

  Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  abstract Either<String, MatchingSuccess> tryMatch(TypeElement mapperClass);

  abstract NonFlagSkew skew();
}
