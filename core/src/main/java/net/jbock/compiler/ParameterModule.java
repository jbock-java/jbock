package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.convert.matching.matcher.ExactMatcher;
import net.jbock.convert.matching.matcher.ListMatcher;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.matching.matcher.OptionalMatcher;
import net.jbock.qualifier.ConverterClass;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;
import net.jbock.scope.ParameterScope;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Module
public class ParameterModule {

  private final AnnotationUtil annotationUtil = new AnnotationUtil();

  private final TypeTool tool;
  private final SourceElement sourceElement;
  private final Util util;

  public ParameterModule(
      TypeTool tool,
      SourceElement sourceElement,
      Util util) {
    this.tool = tool;
    this.sourceElement = sourceElement;
    this.util = util;
  }

  @ParameterScope
  @Provides
  EnumName enumName(
      SourceMethod sourceMethod,
      List<ConvertedParameter<NamedOption>> alreadyCreatedOptions,
      List<ConvertedParameter<PositionalParameter>> alreadyCreatedParams) {
    String methodName = sourceMethod.method().getSimpleName().toString();
    EnumName originalName = EnumName.create(methodName);
    Set<String> alreadyCreated = util.concat(alreadyCreatedOptions, alreadyCreatedParams)
        .stream()
        .map(ConvertedParameter::enumName)
        .map(EnumName::enumConstant)
        .collect(Collectors.toSet());
    EnumName result = originalName;
    while (alreadyCreated.contains(result.enumConstant())) {
      result = originalName.makeLonger();
    }
    return result;
  }

  @ParameterScope
  @Provides
  List<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return List.of(optionalMatcher, listMatcher, exactMatcher);
  }

  @ParameterScope
  @Provides
  TypeTool tool() {
    return tool;
  }

  @ParameterScope
  @Provides
  Types types() {
    return tool.types();
  }

  @ParameterScope
  @Provides
  Elements elements() {
    return tool.elements();
  }

  @ParameterScope
  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @ParameterScope
  @Provides
  ConverterClass converter(SourceMethod sourceMethod) {
    return new ConverterClass(annotationUtil.getConverter(sourceMethod.method()));
  }

  @ParameterScope
  @Provides
  Util util() {
    return new Util();
  }

  @ParameterScope
  @Provides
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return sourceMethod.style();
  }
}
