package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.MatchFactory;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

import static net.jbock.compiler.TypeTool.AS_PRIMITIVE;

@Reusable
public class ExactMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final Types types;
  private final MatchFactory matchFactory;

  @Inject
  ExactMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      Types types,
      MatchFactory matchFactory) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.types = types;
    this.matchFactory = matchFactory;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    Match match = matchFactory.create(boxedReturnType(), constructorParam, Skew.REQUIRED);
    return Optional.of(match);
  }

  private TypeMirror boxedReturnType() {
    TypeMirror sourceType = sourceMethod.returnType();
    PrimitiveType primitive = AS_PRIMITIVE.visit(sourceType);
    return primitive == null ? sourceType :
        types.boxedClass(primitive).asType();
  }
}
