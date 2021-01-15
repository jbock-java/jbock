package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import dagger.Reusable;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@Reusable
public class ParameterContext {

  final ExecutableElement sourceMethod;
  final TypeElement sourceElement;
  final TypeTool tool;
  final ClassName optionType;
  final ImmutableList<Parameter> alreadyCreated;
  final String[] description;
  final String bundleKey;
  final EnumName enumName;

  @Inject
  ParameterContext(
      ExecutableElement sourceMethod,
      @SourceElement TypeElement sourceElement,
      TypeTool tool,
      ClassName optionType,
      ImmutableList<Parameter> alreadyCreated,
      String[] description,
      @BundleKey String bundleKey,
      EnumName enumName) {
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.tool = tool;
    this.optionType = optionType;
    this.alreadyCreated = alreadyCreated;
    this.description = description;
    this.bundleKey = bundleKey;
    this.enumName = enumName;
  }
}
