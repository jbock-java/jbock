package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.matching.matcher.ExactMatcher;
import net.jbock.convert.matching.matcher.ListMatcher;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.matching.matcher.OptionalMatcher;
import net.jbock.qualifier.ConverterClass;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

@Module
class ParameterModule {

  private final AnnotationUtil annotationUtil = new AnnotationUtil();

  private final GeneratedType generatedType;
  private final TypeTool tool;
  private final SourceElement sourceElement;
  private final DescriptionBuilder descriptionBuilder;

  ParameterModule(
      GeneratedType generatedType,
      TypeTool tool,
      SourceElement sourceElement,
      DescriptionBuilder descriptionBuilder) {
    this.generatedType = generatedType;
    this.tool = tool;
    this.sourceElement = sourceElement;
    this.descriptionBuilder = descriptionBuilder;
  }

  @Reusable
  @Provides
  EnumName enumName(
      SourceMethod sourceMethod,
      List<ConvertedParameter<NamedOption>> alreadyCreatedOptions,
      List<ConvertedParameter<PositionalParameter>> alreadyCreatedParams) {
    String methodName = sourceMethod.method().getSimpleName().toString();
    EnumName originalName = EnumName.create(methodName);
    EnumName result = originalName;
    int counter = 2;
    while (!isFresh(result, alreadyCreatedOptions, alreadyCreatedParams)) {
      result = originalName.append(counter++);
    }
    return result;
  }

  private boolean isFresh(
      EnumName result,
      List<ConvertedParameter<NamedOption>> alreadyCreatedOptions,
      List<ConvertedParameter<PositionalParameter>> alreadyCreatedParams) {
    for (ConvertedParameter<NamedOption> c : alreadyCreatedOptions) {
      if (c.enumName().enumConstant().equals(result.enumConstant())) {
        return false;
      }
    }
    for (ConvertedParameter<PositionalParameter> c : alreadyCreatedParams) {
      if (c.enumName().enumConstant().equals(result.enumConstant())) {
        return false;
      }
    }
    return true;
  }

  @Reusable
  @Provides
  List<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }

  @Provides
  GeneratedType generatedType() {
    return generatedType;
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
  DescriptionKey descriptionKey(SourceMethod sourceMethod, ParameterStyle parameterStyle) {
    return new DescriptionKey(parameterStyle.getDescriptionKey(sourceMethod.method()));
  }

  @Reusable
  @Provides
  ConverterClass converter(SourceMethod sourceMethod) {
    return new ConverterClass(annotationUtil.getConverter(sourceMethod.method()));
  }

  @Reusable
  @Provides
  ParamLabel paramLabel(SourceMethod sourceMethod, ParameterStyle parameterStyle) {
    return new ParamLabel(parameterStyle.getParamLabel(sourceMethod.method()));
  }

  @Reusable
  @Provides
  Description description(SourceMethod sourceMethod) {
    return descriptionBuilder.getDescription(sourceMethod.method());
  }

  @Reusable
  @Provides
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return sourceMethod.style();
  }
}
