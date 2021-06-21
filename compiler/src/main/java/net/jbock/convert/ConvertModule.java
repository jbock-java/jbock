package net.jbock.convert;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.matcher.ExactMatcher;
import net.jbock.convert.matcher.ListMatcher;
import net.jbock.convert.matcher.Matcher;
import net.jbock.convert.matcher.OptionalMatcher;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ParameterStyle;

import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Module
public class ConvertModule {

  private final AnnotationUtil annotationUtil = new AnnotationUtil();

  private final TypeTool tool;
  private final SourceElement sourceElement;
  private final Util util;
  private final Types types;
  private final SafeElements elements;

  public ConvertModule(
      TypeTool tool,
      Types types,
      SourceElement sourceElement,
      Util util,
      SafeElements elements) {
    this.tool = tool;
    this.sourceElement = sourceElement;
    this.util = util;
    this.elements = elements;
    this.types = types;
  }

  @ParameterScope
  @Provides
  EnumName enumName(
      SourceMethod sourceMethod,
      List<Mapped<NamedOption>> alreadyCreatedOptions,
      List<Mapped<PositionalParameter>> alreadyCreatedParams) {
    String methodName = sourceMethod.method().getSimpleName().toString();
    EnumName originalName = EnumName.create(methodName);
    Set<String> alreadyCreated = util.concat(alreadyCreatedOptions, alreadyCreatedParams)
        .stream()
        .map(Mapped::enumName)
        .map(EnumName::enumConstant)
        .collect(Collectors.toSet());
    EnumName result = originalName;
    for (int i = 0; i < 100 && alreadyCreated.contains(result.enumConstant()); i++) {
      result = result.makeLonger();
    }
    if (alreadyCreated.contains(result.enumConstant())) {
      throw new AssertionError();
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
  SafeElements elements() {
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
  ParameterStyle parameterStyle(SourceMethod sourceMethod) {
    return sourceMethod.style();
  }
}
