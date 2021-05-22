package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
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

  @Reusable
  @Provides
  EnumName enumName(
      SourceMethod sourceMethod,
      List<ConvertedParameter<NamedOption>> alreadyCreatedOptions,
      List<ConvertedParameter<PositionalParameter>> alreadyCreatedParams) {
    String methodName = sourceMethod.method().getSimpleName().toString();
    EnumName originalName = EnumName.create(methodName);
    Set<List<String>> alreadyCreated = util.concat(alreadyCreatedOptions, alreadyCreatedParams)
        .stream()
        .map(ConvertedParameter::enumName)
        .map(EnumName::parts)
        .collect(Collectors.toSet());
    EnumName result = originalName;
    while (alreadyCreated.contains(result.parts())) {
      result = originalName.makeLonger();
    }
    return result;
  }

  @Reusable
  @Provides
  List<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return List.of(optionalMatcher, listMatcher, exactMatcher);
  }

  @Provides
  TypeTool tool() {
    return tool;
  }

  @Provides
  Types types() {
    return tool.types();
  }

  @Provides
  Elements elements() {
    return tool.elements();
  }

  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @Reusable
  @Provides
  ConverterClass converter(SourceMethod sourceMethod) {
    return new ConverterClass(annotationUtil.getConverter(sourceMethod.method()));
  }

  @Reusable
  @Provides
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return sourceMethod.style();
  }
}
