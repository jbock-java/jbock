package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    if (parameter.style() == ParameterStyle.PARAMETERS) {
      // @Parameters doesn't do required
      return Optional.empty();
    }
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    Match match = Match.create(boxedReturnType(), constructorParam, Skew.REQUIRED, tailExpr(parameter));
    return Optional.of(match);
  }

  private CodeBlock tailExpr(AbstractParameter parameter) {
    List<String> dashedNames = parameter.dashedNames();
    String enumConstant = parameter.enumName().enumConstant();
    CodeBlock.Builder code = CodeBlock.builder();
    if (parameter.isOption()) {
      code.add("\n.findAny()");
    }
    String name = dashedNames.isEmpty() ?
        enumConstant :
        enumConstant + " (" + String.join(", ", dashedNames) + ")";
    return code.add("\n.orElseThrow(() -> missingRequired($S))", name)
        .build();
  }
}
