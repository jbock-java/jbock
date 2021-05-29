package net.jbock.convert;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.EnumName;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.compiler.SourceElement;
import net.jbock.convert.matching.matcher.ExactMatcher;
import net.jbock.convert.matching.matcher.ListMatcher;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.matching.matcher.OptionalMatcher;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.ParameterStyle;
import net.jbock.parameter.PositionalParameter;
import net.jbock.validate.SourceMethod;

import javax.annotation.processing.Messager;
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
  private final Types types;
  private final Elements elements;
  private final Messager messager;

  public ParameterModule(
      TypeTool tool,
      Types types,
      SourceElement sourceElement,
      Util util,
      Elements elements,
      Messager messager) {
    this.tool = tool;
    this.sourceElement = sourceElement;
    this.util = util;
    this.elements = elements;
    this.messager = messager;
    this.types = types;
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
    return types;
  }

  @ParameterScope
  @Provides
  Elements elements() {
    return elements;
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
  Util util(TypeTool tool) {
    return new Util(types, tool);
  }

  @ParameterScope
  @Provides
  Messager messager() {
    return messager;
  }

  @ParameterScope
  @Provides
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return sourceMethod.style();
  }
}
