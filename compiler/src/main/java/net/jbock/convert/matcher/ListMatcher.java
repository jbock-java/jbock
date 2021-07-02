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
public class ListMatcher implements Matcher {

  private final SourceMethod sourceMethod;
  private final SafeElements elements;
  private final TypeTool tool;

  @Inject
  ListMatcher(
      SourceMethod sourceMethod,
      SafeElements elements,
      TypeTool tool) {
    this.sourceMethod = sourceMethod;
    this.elements = elements;
    this.tool = tool;
  }

  @Override
  public Optional<Match> tryMatch(AbstractItem parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    return elements.getTypeElement("java.util.List")
        .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
            .map(typeArg -> Match.create(typeArg, Multiplicity.REPEATABLE)));
  }
}
