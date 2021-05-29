package net.jbock.validate;

import com.squareup.javapoet.TypeSpec;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.compiler.SourceElement;
import net.jbock.context.ContextModule;
import net.jbock.context.DaggerContextComponent;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.DaggerParameterComponent;
import net.jbock.convert.ParameterModule;
import net.jbock.either.Either;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.either.Either.left;

@ValidateScope
public class CommandProcessor {

  private final SourceElement sourceElement;
  private final Elements elements;
  private final TypeTool tool;
  private final Types types;
  private final ParamsFactory paramsFactory;
  private final MethodsFactory methodsFactory;
  private final Util util;
  private final Messager messager;

  @Inject
  CommandProcessor(
      SourceElement sourceElement,
      Elements elements,
      TypeTool tool,
      Types types,
      ParamsFactory paramsFactory,
      MethodsFactory methodsFactory,
      Util util,
      Messager messager) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.tool = tool;
    this.types = types;
    this.paramsFactory = paramsFactory;
    this.methodsFactory = methodsFactory;
    this.util = util;
    this.messager = messager;
  }

  public Either<List<ValidationFailure>, TypeSpec> generate() {
    return methodsFactory.findAbstractMethods()
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

  private Either<List<ValidationFailure>, IntermediateResult> createPositionalParams(AbstractMethods methods) {
    List<ConvertedParameter<PositionalParameter>> positionalParams = new ArrayList<>(methods.positionalParameters().size());
    List<ValidationFailure> failures = new ArrayList<>();
    for (SourceMethod sourceMethod : methods.positionalParameters()) {
      DaggerParameterComponent.builder()
          .module(parameterModule())
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(positionalParams)
          .alreadyCreatedOptions(List.of())
          .build()
          .positionalParameterFactory()
          .createPositionalParam(sourceMethod.index().orElse(methods.positionalParameters().size() - 1))
          .accept(failures::add, positionalParams::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return IntermediateResult.create(methods.namedOptions(), positionalParams);
  }

  private ParameterModule parameterModule() {
    return new ParameterModule(tool, types, sourceElement, util, elements, messager);
  }

  private ContextModule contextModule(Params params) {
    return new ContextModule(sourceElement, elements, params, types);
  }
}
