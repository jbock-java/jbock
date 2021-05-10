package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.matching.matcher.ExactMatcher;
import net.jbock.convert.matching.matcher.ListMatcher;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.matching.matcher.OptionalMatcher;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ConverterClass;
import net.jbock.qualifier.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@Module
class ParameterModule {

  private final AnnotationUtil annotationUtil = new AnnotationUtil();

  private final ClassName optionType;
  private final TypeTool tool;
  private final ParserFlavour flavour;
  private final TypeElement sourceElement;
  private final DescriptionBuilder descriptionBuilder;

  ParameterModule(
      ClassName optionType,
      TypeTool tool,
      ParserFlavour flavour,
      TypeElement sourceElement,
      DescriptionBuilder descriptionBuilder) {
    this.optionType = optionType;
    this.tool = tool;
    this.flavour = flavour;
    this.sourceElement = sourceElement;
    this.descriptionBuilder = descriptionBuilder;
  }

  @Reusable
  @Provides
  EnumName enumName(
      ExecutableElement sourceMethod,
      ImmutableList<ConvertedParameter<NamedOption>> alreadyCreated) {
    String methodName = sourceMethod.getSimpleName().toString();
    EnumName result = EnumName.create(methodName);
    for (ConvertedParameter<NamedOption> param : alreadyCreated) {
      if (param.enumName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  @Reusable
  @Provides
  ImmutableList<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }

  @Provides
  ClassName optionType() {
    return optionType;
  }

  @Provides
  TypeTool tool() {
    return tool;
  }

  @Provides
  ParserFlavour flavour() {
    return flavour;
  }

  @Reusable
  @Provides
  SourceElement sourceElement() {
    return new SourceElement(sourceElement);
  }

  @Reusable
  @Provides
  DescriptionKey descriptionKey(ExecutableElement sourceMethod) {
    return new DescriptionKey(getParameterDescriptionKey(sourceMethod));
  }

  @Reusable
  @Provides
  ConverterClass converter(ExecutableElement sourceMethod) {
    return new ConverterClass(annotationUtil.getConverter(sourceMethod));
  }

  @Reusable
  @Provides
  Description description(ExecutableElement sourceMethod) {
    return descriptionBuilder.getDescription(sourceMethod);
  }

  private String getParameterDescriptionKey(ExecutableElement method) {
    Parameter parameter = method.getAnnotation(Parameter.class);
    if (parameter != null) {
      return parameter.descriptionKey();
    }
    Option option = method.getAnnotation(Option.class);
    if (option != null) {
      return option.descriptionKey();
    }
    Parameters parameters = method.getAnnotation(Parameters.class);
    if (parameters != null) {
      return parameters.descriptionKey();
    }
    return null;
  }
}
