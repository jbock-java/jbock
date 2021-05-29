package net.jbock.convert.matching.matcher;

import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractParameter;
import net.jbock.validate.SourceMethod;

import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Optional;

@ParameterScope
public class OptionalMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final Elements elements;
  private final Messager messager;

  @Inject
  OptionalMatcher(
      SourceMethod sourceMethod,
      TypeTool tool,
      Elements elements,
      Messager messager) {
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.elements = elements;
    this.messager = messager;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    return getOptionalPrimitive(returnType).or(() ->
        tool.getSingleTypeArgument(returnType, Optional.class)
            .map(typeArg -> Match.create(typeArg, Skew.OPTIONAL)));
  }

  private Optional<Match> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool.isSameType(type, optionalPrimitive.type())) {
        String wrapped = optionalPrimitive.wrappedObjectType();
        TypeElement el = elements.getTypeElement(wrapped);
        if (el == null) {
          // should not happen
          messager.printMessage(Diagnostic.Kind.WARNING,
              "TypeElement not found: " + wrapped);
          return Optional.empty();
        }
        TypeMirror baseType = el.asType();
        return Optional.of(Match.create(baseType,
            Skew.OPTIONAL, optionalPrimitive.extractExpr()));
      }
    }
    return Optional.empty();
  }
}
