package net.jbock.convert.matching.matcher;

import net.jbock.convert.ParameterScope;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractParameter;
import net.jbock.validate.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_PRIMITIVE;

@ParameterScope
public class ExactMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final Types types;

  @Inject
  ExactMatcher(
      SourceMethod sourceMethod,
      Types types) {
    this.sourceMethod = sourceMethod;
    this.types = types;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    Match match = Match.create(boxedReturnType(), Skew.REQUIRED);
    return Optional.of(match);
  }

  private TypeMirror boxedReturnType() {
    TypeMirror sourceType = sourceMethod.returnType();
    PrimitiveType primitive = AS_PRIMITIVE.visit(sourceType);
    return primitive == null ? sourceType :
        types.boxedClass(primitive).asType();
  }
}
