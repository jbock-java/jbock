package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class ListMatcher extends Matcher {

  @Inject
  ListMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    ParameterSpec constructorParam = constructorParam(returnType());
    return tool().getSingleTypeArgument(returnType(), List.class)
        .map(typeArg -> Match.create(typeArg, constructorParam, Skew.REPEATABLE));
  }
}
