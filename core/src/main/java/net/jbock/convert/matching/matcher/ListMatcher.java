package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.MatchFactory;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

@Reusable
public class ListMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;
  private final MatchFactory matchFactory;

  @Inject
  ListMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      TypeTool tool,
      MatchFactory matchFactory) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.tool = tool;
    this.matchFactory = matchFactory;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    ParameterSpec constructorParam = constructorParam(returnType);
    return tool.getSingleTypeArgument(returnType, List.class)
        .map(typeArg -> matchFactory.create(typeArg, constructorParam, Skew.REPEATABLE));
  }
}
