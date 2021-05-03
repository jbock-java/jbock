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
    return Optional.of(Match.create(boxedReturnType(), constructorParam, Skew.REQUIRED, tailExpr(parameter)));
  }

  private CodeBlock tailExpr(AbstractParameter parameter) {
    List<String> dashedNames = parameter.dashedNames();
    String enumConstant = parameter.enumName().enumConstant();
    String s = dashedNames.isEmpty() ?
        enumConstant :
        enumConstant + " (" + String.join(", ", dashedNames) + ")";
    CodeBlock.Builder code = CodeBlock.builder();
    if (!parameter.isPositional()) {
      code.add("\n.findAny()", s);
    }
    return code.add("\n.orElseThrow(() -> missingRequired($S))", s)
        .build();
  }
}
