package net.jbock.convert.matcher;

import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matching.Match;
import net.jbock.either.Optional;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;

@ParameterScope
public class OptionalMatcher implements Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final SafeElements elements;

  @Inject
  OptionalMatcher(
      SourceMethod sourceMethod,
      TypeTool tool,
      SafeElements elements) {
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.elements = elements;
  }

  @Override
  public Optional<Match> tryMatch(AbstractItem parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    return getOptionalPrimitive(returnType)
        .or(() -> // TODO support other kinds of Optional
            tool.getSingleTypeArgument(returnType, java.util.Optional.class)
                .map(typeArg -> Match.create(typeArg, Multiplicity.OPTIONAL)));
  }

  private Optional<Match> getOptionalPrimitive(TypeMirror type) {
    for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
      if (tool.isSameType(type, optionalPrimitive.type())) {
        String wrapped = optionalPrimitive.wrappedObjectType();
        return elements.getTypeElement(wrapped)
            .flatMap(el -> {
              TypeMirror baseType = el.asType();
              return Optional.of(Match.create(baseType,
                  Multiplicity.OPTIONAL, optionalPrimitive.extractExpr()));
            });
      }
    }
    return Optional.empty();
  }
}
