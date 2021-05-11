package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import dagger.Reusable;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

@Reusable
public class ParameterContext {

  final TypeElement sourceElement;
  final TypeTool tool;
  final ClassName optionType;
  final Description description;
  final ParserFlavour flavour;

  @Inject
  public ParameterContext(
      SourceElement sourceElement,
      TypeTool tool,
      ClassName optionType,
      Description description,
      ParserFlavour flavour) {
    this.sourceElement = sourceElement.element();
    this.tool = tool;
    this.optionType = optionType;
    this.description = description;
    this.flavour = flavour;
  }
}
