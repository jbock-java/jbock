package net.jbock.convert.matcher;

import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matching.Match;
import net.jbock.either.Optional;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.List;

@ParameterScope
public class ListMatcher implements Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;

  @Inject
  ListMatcher(
      SourceMethod sourceMethod,
      TypeTool tool) {
    this.sourceMethod = sourceMethod;
    this.tool = tool;
  }

  @Override
  public Optional<Match> tryMatch(AbstractItem parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    return tool.getSingleTypeArgument(returnType, List.class)
        .map(typeArg -> Match.create(typeArg, Multiplicity.REPEATABLE));
  }
}
