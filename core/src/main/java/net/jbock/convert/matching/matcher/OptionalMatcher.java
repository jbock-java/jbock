package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class OptionalMatcher extends Matcher {

  private final SourceMethod sourceMethod;

  @Inject
  OptionalMatcher(
      ParameterContext parameterContext,
      SourceMethod sourceMethod,
      EnumName enumName) {
    super(parameterContext, enumName);
    this.sourceMethod = sourceMethod;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    Optional<Match> optionalPrimitive = getOptionalPrimitive(returnType);
    if (optionalPrimitive.isPresent()) {
      return optionalPrimitive;
    }
    return tool().getSingleTypeArgument(returnType, Optional.class)
        .map(typeArg -> Match.create(typeArg, constructorParam(returnType), Skew.OPTIONAL));
  }

  private Optional<Match> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool().isSameType(type, optionalPrimitive.type())) {
        ParameterSpec constructorParam = constructorParam(asOptional(optionalPrimitive));
        return Optional.of(Match.create(
            tool().asTypeElement(optionalPrimitive.wrappedObjectType()).asType(),
            constructorParam,
            Skew.OPTIONAL,
            optionalPrimitive.extractExpr(constructorParam)));
      }
    }
    return Optional.empty();
  }

  private DeclaredType asOptional(OptionalPrimitive optionalPrimitive) {
    TypeElement optional = tool().asTypeElement(Optional.class.getCanonicalName());
    TypeElement element = tool().asTypeElement(optionalPrimitive.wrappedObjectType());
    return tool().types().getDeclaredType(optional, element.asType());
  }
}
