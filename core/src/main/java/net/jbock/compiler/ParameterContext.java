package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.qualifier.BundleKey;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@Reusable
public class ParameterContext {

  final ExecutableElement sourceMethod;
  final TypeElement sourceElement;
  final TypeTool tool;
  final ClassName optionType;
  final ImmutableList<PositionalParameter> alreadyCreatedParams;
  final ImmutableList<NamedOption> alreadyCreatedOptions;
  final Description description;
  final String bundleKey;
  final EnumName enumName;
  final ParserFlavour flavour;

  @Inject
  public ParameterContext(
      ExecutableElement sourceMethod,
      @SourceElement TypeElement sourceElement,
      TypeTool tool,
      ClassName optionType,
      ImmutableList<PositionalParameter> alreadyCreatedParams,
      ImmutableList<NamedOption> alreadyCreatedOptions,
      Description description,
      @BundleKey String bundleKey,
      EnumName enumName,
      ParserFlavour flavour) {
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.tool = tool;
    this.optionType = optionType;
    this.alreadyCreatedParams = alreadyCreatedParams;
    this.alreadyCreatedOptions = alreadyCreatedOptions;
    this.description = description;
    this.bundleKey = bundleKey;
    this.enumName = enumName;
    this.flavour = flavour;
  }
}
