package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

@Reusable
public class ParameterContext {

  final TypeElement sourceElement;
  final TypeTool tool;
  final ClassName optionType;
  final ImmutableList<ConvertedParameter<PositionalParameter>> alreadyCreatedParams;
  final ImmutableList<ConvertedParameter<NamedOption>> alreadyCreatedOptions;
  final Description description;
  final EnumName enumName;
  final ParserFlavour flavour;

  @Inject
  public ParameterContext(
      SourceElement sourceElement,
      TypeTool tool,
      ClassName optionType,
      ImmutableList<ConvertedParameter<PositionalParameter>> alreadyCreatedParams,
      ImmutableList<ConvertedParameter<NamedOption>> alreadyCreatedOptions,
      Description description,
      EnumName enumName,
      ParserFlavour flavour) {
    this.sourceElement = sourceElement.element();
    this.tool = tool;
    this.optionType = optionType;
    this.alreadyCreatedParams = alreadyCreatedParams;
    this.alreadyCreatedOptions = alreadyCreatedOptions;
    this.description = description;
    this.enumName = enumName;
    this.flavour = flavour;
  }
}
