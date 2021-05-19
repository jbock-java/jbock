package net.jbock.compiler.command;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.AbstractMethods;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class CommandProcessor {

  private final SourceElement sourceElement;
  private final Elements elements;
  private final Types types;
  private final TypeTool tool;
  private final DescriptionBuilder descriptionBuilder;
  private final ParamsFactory paramsFactory;
  private final MethodsFactory methodsFactory;
  private final ParameterMethodValidator parameterMethodValidator;

  @Inject
  CommandProcessor(
      SourceElement sourceElement,
      Elements elements,
      Types types,
      TypeTool tool,
      DescriptionBuilder descriptionBuilder,
      ParamsFactory paramsFactory,
      MethodsFactory methodsFactory,
      ParameterMethodValidator parameterMethodValidator) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.types = types;
    this.tool = tool;
    this.descriptionBuilder = descriptionBuilder;
    this.paramsFactory = paramsFactory;
    this.methodsFactory = methodsFactory;
    this.parameterMethodValidator = parameterMethodValidator;
  }

  public Either<List<ValidationFailure>, TypeSpec> generate() {
    return createMethods()
        .flatMap(this::getPositionalParams)
        .flatMap(this::getNamedOptions)
        .map(params -> new ContextModule(sourceElement, elements, params))
        .map(module -> DaggerContextComponent.factory()
            .create(module)
            .generatedClass()
            .define());
  }

  private Either<List<ValidationFailure>, Params> getNamedOptions(IntermediateResult intermediateResult) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ConvertedParameter<NamedOption>> optionsBuilder = new ArrayList<>(intermediateResult.options().size());
    for (SourceMethod sourceMethod : intermediateResult.options()) {
      DaggerParameterComponent.builder()
          .module(parameterModule())
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(intermediateResult.positionalParameters())
          .alreadyCreatedOptions(optionsBuilder)
          .build()
          .namedOptionFactory()
          .createNamedOption()
          .accept(failures::add, optionsBuilder::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return paramsFactory.create(intermediateResult.positionalParameters(), optionsBuilder);
  }

  private Either<List<ValidationFailure>, IntermediateResult> getPositionalParams(Methods methods) {
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

  private Either<List<ValidationFailure>, Methods> createMethods() {
    return findRelevantMethods(sourceElement.element().asType())
        .flatMap(this::validateAtLeastOneAbstractMethod)
        .flatMap(this::validateParameterMethods)
        .flatMap(methodsFactory::create);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> validateParameterMethods(
      List<ExecutableElement> sourceMethods) {
    List<ValidationFailure> failures = new ArrayList<>();
    for (ExecutableElement sourceMethod : sourceMethods) {
      parameterMethodValidator.validateParameterMethod(sourceMethod)
          .map(msg -> new ValidationFailure(msg, sourceMethod))
          .ifPresent(failures::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(sourceMethods);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> validateAtLeastOneAbstractMethod(
      List<ExecutableElement> sourceMethods) {
    if (sourceMethods.isEmpty()) { // javapoet #739
      return left(Collections.singletonList(sourceElement.fail("expecting at least one abstract method")));
    }
    return right(sourceMethods);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods(TypeMirror sourceElement) {
    List<ExecutableElement> acc = new ArrayList<>();
    while (true) {
      Either<List<ValidationFailure>, TypeElement> element = findRelevantMethods(sourceElement, acc);
      if (!element.isRight()) {
        List<ValidationFailure> failures = element.fold(
            Function.identity(),
            __ -> Collections.emptyList());
        if (!failures.isEmpty()) {
          return left(failures);
        } else {
          break;
        }
      }
      sourceElement = element.fold(
          __ -> null,
          Function.identity())
          .getSuperclass();
    }
    Map<Boolean, List<ExecutableElement>> map = acc.stream()
        .collect(Collectors.partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
    return right(AbstractMethods.create(map.get(true), map.get(false), types)
        .unimplementedAbstract());
  }

  private Either<List<ValidationFailure>, TypeElement> findRelevantMethods(
      TypeMirror sourceElement, List<ExecutableElement> acc) {
    if (sourceElement.getKind() != TypeKind.DECLARED) {
      return left(Collections.emptyList());
    }
    DeclaredType declared = AS_DECLARED.visit(sourceElement);
    if (declared == null) {
      return left(Collections.emptyList());
    }
    TypeElement typeElement = AS_TYPE_ELEMENT.visit(declared.asElement());
    if (typeElement == null) {
      return left(Collections.emptyList());
    }
    if (!typeElement.getModifiers().contains(ABSTRACT)) {
      return left(Collections.emptyList());
    }
    List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
    if (!interfaces.isEmpty()) {
      return left(Collections.singletonList(
          new ValidationFailure("this abstract class may not implement any interfaces", typeElement)));
    }
    acc.addAll(methodsIn(typeElement.getEnclosedElements()));
    return right(typeElement);
  }

  private ParameterModule parameterModule() {
    return new ParameterModule(tool, sourceElement, descriptionBuilder);
  }
}
