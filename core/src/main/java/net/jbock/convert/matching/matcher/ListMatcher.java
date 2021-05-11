package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class ListMatcher extends Matcher {

  private final SourceMethod sourceMethod;
  private final TypeTool tool;

  @Inject
  ListMatcher(
      SourceMethod sourceMethod,
      EnumName enumName,
      TypeTool tool) {
    super(enumName);
    this.sourceMethod = sourceMethod;
    this.tool = tool;
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    TypeMirror returnType = sourceMethod.returnType();
    ParameterSpec constructorParam = constructorParam(returnType);
    return tool.getSingleTypeArgument(returnType, List.class)
        .map(typeArg -> Match.create(typeArg, constructorParam, Skew.REPEATABLE));
  }
}
