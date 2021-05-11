package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final Types types;

  @Inject
  ExactMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      Types types) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.types = types;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    Match match = Match.create(boxedReturnType(), constructorParam, Skew.REQUIRED);
    return Optional.of(match);
  }

  private TypeMirror boxedReturnType() {
    TypeMirror sourceType = sourceMethod.returnType();
    PrimitiveType primitive = sourceType.accept(TypeTool.AS_PRIMITIVE, null);
    return primitive == null ? sourceType :
        types.boxedClass(primitive).asType();
  }
}
