package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListMatcher extends Matcher {

  @Inject
  ListMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    if (parameter.style() == ParameterStyle.PARAMETER) {
      // @Parameter doesn't do lists
      return Optional.empty();
    }
    ParameterSpec constructorParam = constructorParam(returnType());
    return tool().getSingleTypeArgument(returnType(), List.class)
        .map(typeArg -> Match.create(typeArg, constructorParam, Skew.REPEATABLE, tailExpr()));
  }

  private CodeBlock tailExpr() {
    return CodeBlock.of(".collect($T.toList())", Collectors.class);
  }
}
