package net.jbock.compiler.command;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.compiler.AbstractMethods;
import net.jbock.compiler.ContextModule;
import net.jbock.compiler.DaggerContextComponent;
import net.jbock.compiler.DaggerParameterComponent;
import net.jbock.compiler.DescriptionBuilder;
import net.jbock.compiler.Methods;
import net.jbock.compiler.ParameterModule;
import net.jbock.compiler.Params;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationFailure;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
  private final Util util;
  private final Types types;
  private final TypeTool tool;
  private final DescriptionBuilder descriptionBuilder;

  @Inject
  CommandProcessor(
      SourceElement sourceElement,
      Elements elements,
      Util util,
      Types types,
      TypeTool tool,
      DescriptionBuilder descriptionBuilder) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.util = util;
    this.types = types;
    this.tool = tool;
    this.descriptionBuilder = descriptionBuilder;
  }

  public Either<List<ValidationFailure>, TypeSpec> generate() {
    return Either.ofLeft(validateSourceElement()).orRight(null)
        .mapLeft(sourceElement::fail)
        .mapLeft(Collections::singletonList)
        .flatMap(nothing -> getParams())
        .map(params -> {
          ContextModule module = new ContextModule(sourceElement, elements, params);
          return DaggerContextComponent.factory()
              .create(module)
              .generatedClass()
              .define();
        });
  }

  private Either<List<ValidationFailure>, Params> getParams() {
    ParameterModule module = new ParameterModule(tool, sourceElement, descriptionBuilder);
    return createMethods(sourceElement).flatMap(methods -> getPositionalParams(module, methods.params())
        .flatMap(positionalParams -> getNamedOptions(module, methods.options(), positionalParams)
            .flatMap(namedOptions -> {
              List<ValidationFailure> failures = checkDuplicateDescriptionKeys(namedOptions, positionalParams);
              if (!failures.isEmpty()) {
                return left(failures);
              }
              return right(Params.create(positionalParams, namedOptions));
            })));
  }

  private Either<List<ValidationFailure>, List<ConvertedParameter<NamedOption>>> getNamedOptions(
      ParameterModule module,
      List<SourceMethod> options,
      List<ConvertedParameter<PositionalParameter>> positionalParams) {
    List<ValidationFailure> failures = new ArrayList<>();
    ImmutableList.Builder<ConvertedParameter<NamedOption>> optionsBuilder = ImmutableList.builder();
    for (SourceMethod sourceMethod : options) {
      DaggerParameterComponent.builder()
          .module(module)
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(positionalParams)
          .alreadyCreatedOptions(optionsBuilder.build())
          .build()
          .namedOptionFactory()
          .createNamedOption()
          .accept(failures::add, optionsBuilder::add);
    }
    if (failures.isEmpty()) {
      return right(optionsBuilder.build());
    }
    return left(failures);
  }

  private Either<List<ValidationFailure>, List<ConvertedParameter<PositionalParameter>>> getPositionalParams(
      ParameterModule module, List<SourceMethod> positionalParameters) {
    ImmutableList.Builder<ConvertedParameter<PositionalParameter>> positionalParams =
        ImmutableList.builder();
    List<ValidationFailure> failures = new ArrayList<>();
    for (SourceMethod sourceMethod : positionalParameters) {
      DaggerParameterComponent.builder()
          .module(module)
          .sourceMethod(sourceMethod)
          .alreadyCreatedParams(positionalParams.build())
          .alreadyCreatedOptions(ImmutableList.of())
          .build()
          .positionalParameterFactory()
          .createPositionalParam(sourceMethod.index().orElse(positionalParameters.size() - 1))
          .accept(failures::add, positionalParams::add);
    }
    if (sourceElement.isSuperCommand() && positionalParameters.isEmpty()) {
      String message = "in a @" + SuperCommand.class.getSimpleName() +
          ", at least one @" + Parameter.class.getSimpleName() + " must be defined";
      failures.add(sourceElement.fail(message));
    }
    List<ConvertedParameter<PositionalParameter>> r = positionalParams.build();
    failures.addAll(validatePositions(r));
    if (failures.isEmpty()) {
      return right(r);
    }
    return left(failures);
  }

  private List<ValidationFailure> checkDuplicateDescriptionKeys(
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> positionalParams) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ConvertedParameter<? extends AbstractParameter>> abstractParameters =
        util.concat(namedOptions, positionalParams);
    Set<String> keys = new HashSet<>();
    sourceElement.descriptionKey().ifPresent(keys::add);
    for (ConvertedParameter<? extends AbstractParameter> c : abstractParameters) {
      AbstractParameter p = c.parameter();
      String key = p.descriptionKey().orElse("");
      if (key.isEmpty()) {
        continue;
      }
      if (!keys.add(key)) {
        String message = "duplicate description key: " + key;
        failures.add(p.fail(message));
      }
    }
    return failures;
  }

  private static List<ValidationFailure> validatePositions(List<ConvertedParameter<PositionalParameter>> params) {
    List<ConvertedParameter<PositionalParameter>> sorted = params.stream()
        .sorted(Comparator.comparing(c -> c.parameter().position()))
        .collect(Collectors.toList());
    List<ValidationFailure> result = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      ConvertedParameter<PositionalParameter> c = sorted.get(i);
      PositionalParameter p = c.parameter();
      if (p.position() != i) {
        String message = "Position " + p.position() + " is not available. Suggested position: " + i;
        result.add(p.fail(message));
      }
    }
    return result;
  }

  private Either<List<ValidationFailure>, Methods> createMethods(SourceElement sourceElement) {
    return findRelevantMethods(sourceElement.element().asType()).flatMap(sourceMethods -> {
      List<ValidationFailure> failures = new ArrayList<>();
      for (ExecutableElement sourceMethod : sourceMethods) {
        validateParameterMethod(sourceMethod)
            .map(msg -> new ValidationFailure(msg, sourceMethod))
            .ifPresent(failures::add);
      }
      if (sourceMethods.isEmpty()) { // javapoet #739
        failures.add(sourceElement.fail("expecting at least one abstract method"));
      }
      if (!failures.isEmpty()) {
        return left(failures);
      }
      List<SourceMethod> methods = sourceMethods.stream()
          .map(SourceMethod::create)
          .collect(Collectors.toList());
      return Either.ofLeft(validateDuplicateParametersAnnotation(methods))
          .orRight(Methods.create(methods))
          .mapLeft(Collections::singletonList);
    });
  }

  private Optional<ValidationFailure> validateDuplicateParametersAnnotation(List<SourceMethod> sourceMethods) {
    List<SourceMethod> parametersMethods = sourceMethods.stream()
        .filter(m -> m.style() == ParameterStyle.PARAMETERS)
        .collect(Collectors.toList());
    if (parametersMethods.size() >= 2) {
      String message = "duplicate @" + Parameters.class.getSimpleName() + " annotation";
      return Optional.of(sourceMethods.get(1).fail(message));
    }
    return Optional.empty();
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

  private Optional<String> validateSourceElement() {
    List<String> errors = new ArrayList<>();
    util.commonTypeChecks(sourceElement.element())
        .map(s -> "command " + s)
        .ifPresent(errors::add);
    util.assertNoDuplicateAnnotations(sourceElement.element(), Command.class, SuperCommand.class).ifPresent(errors::add);
    List<? extends TypeMirror> interfaces = sourceElement.element().getInterfaces();
    if (!interfaces.isEmpty()) {
      errors.add("command cannot implement " + interfaces.get(0));
    }
    return errors.stream().findFirst();
  }

  private Optional<String> validateParameterMethod(ExecutableElement sourceMethod) {
    Optional<String> noAnnotationsError = util.assertAtLeastOneAnnotation(sourceMethod,
        Option.class, Parameter.class, Parameters.class);
    if (noAnnotationsError.isPresent()) {
      return noAnnotationsError;
    }
    Optional<String> duplicateAnnotationsError = util.assertNoDuplicateAnnotations(sourceMethod,
        Option.class, Parameter.class, Parameters.class);
    if (duplicateAnnotationsError.isPresent()) {
      return duplicateAnnotationsError;
    }
    if (sourceElement.element().getAnnotation(SuperCommand.class) != null &&
        sourceMethod.getAnnotation(Parameters.class) != null) {
      return Optional.of("@" + Parameters.class.getSimpleName()
          + " cannot be used in a @" + SuperCommand.class.getSimpleName());
    }
    if (!sourceMethod.getParameters().isEmpty()) {
      return Optional.of("empty argument list expected");
    }
    if (!sourceMethod.getTypeParameters().isEmpty()) {
      return Optional.of("type parameter" +
          (sourceMethod.getTypeParameters().size() >= 2 ? "s" : "") +
          " not expected here");
    }
    if (!sourceMethod.getThrownTypes().isEmpty()) {
      return Optional.of("method may not declare any exceptions");
    }
    if (isUnreachable(sourceMethod.getReturnType())) {
      return Optional.of("unreachable type: " + Util.typeToString(sourceMethod.getReturnType()));
    }
    return Optional.empty();
  }

  private static boolean isUnreachable(TypeMirror mirror) {
    TypeKind kind = mirror.getKind();
    if (kind != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    if (declared.asElement().getModifiers().contains(Modifier.PRIVATE)) {
      return true;
    }
    List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
    for (TypeMirror typeArgument : typeArguments) {
      if (isUnreachable(typeArgument)) {
        return true;
      }
    }
    return false;
  }
}
