package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;

public class OptionalMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final Types types;
  private final Elements elements;

  @Inject
  OptionalMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      TypeTool tool,
      Types types,
      Elements elements) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.types = types;
    this.elements = elements;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    Optional<Match> optionalPrimitive = getOptionalPrimitive(returnType);
    if (optionalPrimitive.isPresent()) {
      return optionalPrimitive;
    }
    return tool.getSingleTypeArgument(returnType, Optional.class)
        .map(typeArg -> Match.create(typeArg, constructorParam(returnType), Skew.OPTIONAL));
  }

  private Optional<Match> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool.isSameType(type, optionalPrimitive.type())) {
        ParameterSpec constructorParam = constructorParam(asOptional(optionalPrimitive));
        return Optional.of(Match.create(
            elements.getTypeElement(optionalPrimitive.wrappedObjectType()).asType(),
            constructorParam,
            Skew.OPTIONAL,
            optionalPrimitive.extractExpr(constructorParam)));
      }
    }
    return Optional.empty();
  }

  private DeclaredType asOptional(OptionalPrimitive optionalPrimitive) {
    TypeElement optional = elements.getTypeElement(Optional.class.getCanonicalName());
    TypeElement element = elements.getTypeElement(optionalPrimitive.wrappedObjectType());
    return types.getDeclaredType(optional, element.asType());
  }
}
