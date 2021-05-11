package net.jbock.compiler;

import dagger.Reusable;

import javax.inject.Inject;

@Reusable
public class ParameterContext {

  final TypeTool tool;
  final ParserFlavour flavour;

  @Inject
  public ParameterContext(
      TypeTool tool,
      ParserFlavour flavour) {
    this.tool = tool;
    this.flavour = flavour;
  }
}
