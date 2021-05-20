package net.jbock.compiler.command;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.ContextModule;
import net.jbock.compiler.DaggerContextComponent;
import net.jbock.compiler.DaggerParameterComponent;
import net.jbock.compiler.DescriptionBuilder;
import net.jbock.compiler.Methods;
import net.jbock.compiler.MethodsFactory;
import net.jbock.compiler.ParameterModule;
import net.jbock.compiler.Params;
import net.jbock.compiler.ParamsFactory;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationFailure;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.either.Either.left;

public class CommandProcessor {

  private final SourceElement sourceElement;
  private final Elements elements;
  private final TypeTool tool;
  private final DescriptionBuilder descriptionBuilder;
  private final ParamsFactory paramsFactory;
  private final MethodsFactory methodsFactory;
  private final MethodFinder methodFinder;

  @Inject
  CommandProcessor(
      SourceElement sourceElement,
      Elements elements,
      TypeTool tool,
      DescriptionBuilder descriptionBuilder,
      ParamsFactory paramsFactory,
      MethodsFactory methodsFactory,
      MethodFinder methodFinder) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.tool = tool;
    this.descriptionBuilder = descriptionBuilder;
    this.paramsFactory = paramsFactory;
    this.methodsFactory = methodsFactory;
    this.methodFinder = methodFinder;
  }

  public Either<List<ValidationFailure>, TypeSpec> generate() {
    return methodFinder.findRelevantMethods()
        .flatMap(methodsFactory::create)
        .flatMap(this::createPositionalParams)
        .flatMap(this::createNamedOptions)
        .map(this::contextModule)
        .map(module -> DaggerContextComponent.factory()
            .create(module)
            .generatedClass()
            .define());
  }

  private Either<List<ValidationFailure>, Params> createNamedOptions(
      IntermediateResult intermediateResult) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ConvertedParameter<NamedOption>> namedOptions = new ArrayList<>(intermediateResult.options().size());
    for (SourceMethod sourceMethod : intermediateResult.options()) {
      DaggerParameterComponent.builder()
          .module(parameterModule())
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(intermediateResult.positionalParameters())
          .alreadyCreatedOptions(namedOptions)
          .build()
          .namedOptionFactory()
          .createNamedOption()
          .accept(failures::add, namedOptions::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return paramsFactory.create(intermediateResult.positionalParameters(), namedOptions);
  }

  private Either<List<ValidationFailure>, IntermediateResult> createPositionalParams(Methods methods) {
    List<ConvertedParameter<PositionalParameter>> positionalParams = new ArrayList<>(methods.params().size());
    List<ValidationFailure> failures = new ArrayList<>();
    for (SourceMethod sourceMethod : methods.params()) {
      DaggerParameterComponent.builder()
          .module(parameterModule())
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(positionalParams)
          .alreadyCreatedOptions(ImmutableList.of())
          .build()
          .positionalParameterFactory()
          .createPositionalParam(sourceMethod.index().orElse(methods.params().size() - 1))
          .accept(failures::add, positionalParams::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return IntermediateResult.create(methods.options(), positionalParams);
  }

  private ParameterModule parameterModule() {
    return new ParameterModule(tool, sourceElement, descriptionBuilder);
  }

  private ContextModule contextModule(Params params) {
    return new ContextModule(sourceElement, elements, params);
  }
}
