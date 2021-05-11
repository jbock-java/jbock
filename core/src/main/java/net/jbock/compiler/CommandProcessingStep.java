package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.either.Either;
import net.jbock.qualifier.OptionType;
import net.jbock.qualifier.SourceMethod;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.OperationMode.TEST;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class CommandProcessingStep implements BasicAnnotationProcessor.Step {

  private final TypeTool tool;
  private final Types types;
  private final Messager messager;
  private final Filer filer;
  private final OperationMode operationMode;
  private final DescriptionBuilder descriptionBuilder;
  private final Util util;

  @Inject
  CommandProcessingStep(
      TypeTool tool,
      Types types,
      Messager messager,
      Filer filer,
      OperationMode operationMode,
      DescriptionBuilder descriptionBuilder,
      Util util) {
    this.tool = tool;
    this.types = types;
    this.messager = messager;
    this.filer = filer;
    this.operationMode = operationMode;
    this.descriptionBuilder = descriptionBuilder;
    this.util = util;
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Command.class, SuperCommand.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    elementsByAnnotation.forEach((annotationName, element) -> {
      ParserFlavour flavour = ParserFlavour.forAnnotationName(annotationName);
      ElementFilter.typesIn(Collections.singletonList(element))
          .forEach(sourceElement -> processSourceElement(sourceElement, flavour));
    });
    return Collections.emptySet();
  }

  private void processSourceElement(TypeElement sourceElement, ParserFlavour flavour) {
    ClassName generatedClass = generatedClass(sourceElement);
    try {
      OptionType optionType = new OptionType(generatedClass.nestedClass("Option"));
      Either.ofLeft(validateSourceElement(sourceElement)).orRight(null)
          .mapLeft(msg -> new ValidationFailure(msg, sourceElement))
          .mapLeft(Collections::singletonList)
          .flatMap(nothing -> getParams(sourceElement, flavour, optionType))
          .accept(failures -> {
            for (ValidationFailure failure : failures) {
              messager.printMessage(Diagnostic.Kind.ERROR, failure.message(), failure.about());
            }
          }, parameters -> {
            ContextComponent context = DaggerContextComponent.builder()
                .flavour(flavour)
                .optionType(optionType)
                .sourceElement(sourceElement)
                .generatedClass(generatedClass)
                .options(parameters.namedOptions)
                .params(parameters.positionalParams)
                .description(descriptionBuilder.getDescription(sourceElement))
                .build();
            TypeSpec typeSpec = context.generatedClass().define();
            write(sourceElement, generatedClass, typeSpec);
          });
    } catch (Throwable error) {
      handleUnknownError(sourceElement, error);
    }
  }

  private void write(TypeElement sourceElement, ClassName generatedType, TypeSpec definedType) {
    JavaFile.Builder builder = JavaFile.builder(generatedType.packageName(), definedType);
    JavaFile javaFile = builder.build();
    try {
      JavaFileObject sourceFile = filer.createSourceFile(generatedType.toString(), sourceElement);
      try (Writer writer = sourceFile.openWriter()) {
        String sourceCode = javaFile.toString();
        if (operationMode == TEST) {
          System.out.println("Printing generated code in OperationMode TEST");
          System.err.println(sourceCode);
        }
        writer.write(sourceCode);
      } catch (IOException e) {
        handleUnknownError(sourceElement, e);
      }
    } catch (IOException e) {
      handleUnknownError(sourceElement, e);
    }
  }

  private Either<List<ValidationFailure>, Params> getParams(
      TypeElement sourceElement,
      ParserFlavour flavour,
      OptionType optionType) {
    ParameterModule module = new ParameterModule(optionType, tool,
        flavour, sourceElement, descriptionBuilder);
    return createMethods(sourceElement).flatMap(methods -> {
      ImmutableList.Builder<ConvertedParameter<PositionalParameter>> positionalParamsBuilder =
          ImmutableList.builder();
      List<ValidationFailure> failures = new ArrayList<>();
      List<ExecutableElement> positionalParameters = methods.params();
      for (ExecutableElement sourceMethod : positionalParameters) {
        ParameterComponent.Builder builder = DaggerParameterComponent.builder()
            .module(module)
            .sourceMethod(new SourceMethod(sourceMethod))
            .alreadyCreatedParams(positionalParamsBuilder.build())
            .alreadyCreatedOptions(ImmutableList.of());
        builder.build().positionalParameterFactory().createPositionalParam(
            getIndex(sourceMethod).orElse(positionalParameters.size() - 1))
            .accept(failures::add, positionalParamsBuilder::add);
      }
      if (flavour.isSuperCommand() && positionalParameters.isEmpty()) {
        failures.add(new ValidationFailure("in a @" + SuperCommand.class.getSimpleName() +
            ", at least one @" + Parameter.class.getSimpleName() + " must be defined", sourceElement));
      }
      ImmutableList<ConvertedParameter<PositionalParameter>> positionalParams = positionalParamsBuilder.build();
      failures.addAll(validatePositions(positionalParams));
      ImmutableList.Builder<ConvertedParameter<NamedOption>> namedOptionsBuilder = ImmutableList.builder();
      for (ExecutableElement sourceMethod : methods.options()) {
        ParameterComponent.Builder builder = DaggerParameterComponent.builder()
            .module(module)
            .sourceMethod(new SourceMethod(sourceMethod))
            .alreadyCreatedParams(positionalParams)
            .alreadyCreatedOptions(namedOptionsBuilder.build());
        builder.build().namedOptionFactory().createNamedOption()
            .accept(failures::add, namedOptionsBuilder::add);
      }
      ImmutableList<ConvertedParameter<NamedOption>> namedOptions = namedOptionsBuilder.build();
      failures.addAll(checkDescriptionKeys(namedOptions, positionalParams));
      return failures.isEmpty() ? right(new Params(positionalParams, namedOptions)) : left(failures);
    });
  }

  private List<ValidationFailure> checkDescriptionKeys(
      ImmutableList<ConvertedParameter<NamedOption>> namedOptions,
      ImmutableList<ConvertedParameter<PositionalParameter>> positionalParams) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ConvertedParameter<? extends AbstractParameter>> abstractParameters =
        ImmutableList.<ConvertedParameter<? extends AbstractParameter>>builderWithExpectedSize(namedOptions.size() + positionalParams.size())
            .addAll(positionalParams)
            .addAll(namedOptions)
            .build();
    for (int i = 0; i < abstractParameters.size(); i++) {
      ConvertedParameter<? extends AbstractParameter> c = abstractParameters.get(i);
      checkDescriptionKey(c, abstractParameters.subList(0, i))
          .map(s -> new ValidationFailure(s, c.parameter().sourceMethod()))
          .ifPresent(failures::add);
    }
    return failures;
  }

  private OptionalInt getIndex(ExecutableElement sourceMethod) {
    Parameter parameter = sourceMethod.getAnnotation(Parameter.class);
    if (parameter == null) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(parameter.index());
  }

  private Optional<String> checkDescriptionKey(
      ConvertedParameter<? extends AbstractParameter> p,
      List<ConvertedParameter<? extends AbstractParameter>> alreadyCreated) {
    return p.parameter().descriptionKey().flatMap(key -> {
      if (key.isEmpty()) {
        return Optional.empty();
      }
      for (ConvertedParameter<? extends AbstractParameter> c : alreadyCreated) {
        Optional<String> failure = c.parameter().descriptionKey()
            .filter(descriptionKey -> descriptionKey.equals(key));
        if (failure.isPresent()) {
          return Optional.of("duplicate bundle key: " + key);
        }
      }
      return Optional.empty();
    });
  }

  private static List<ValidationFailure> validatePositions(List<ConvertedParameter<PositionalParameter>> params) {
    List<ConvertedParameter<PositionalParameter>> sorted = params.stream()
        .sorted(Comparator.comparing(c -> c.parameter().position()))
        .collect(Collectors.toList());
    List<ValidationFailure> result = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      ConvertedParameter<PositionalParameter> p = sorted.get(i);
      if (p.parameter().position() != i) {
        result.add(new ValidationFailure("Position " + p.parameter().position() + " is not available." +
            " Suggested position: " + i, p.parameter().sourceMethod()));
      }
    }
    return result;
  }

  private Either<List<ValidationFailure>, Methods> createMethods(TypeElement sourceElement) {
    List<ValidationFailure> failures = new ArrayList<>();
    return findRelevantMethods(sourceElement.asType()).flatMap(sourceMethods -> {
      for (ExecutableElement sourceMethod : sourceMethods) {
        validateParameterMethod(sourceElement, sourceMethod)
            .ifPresent(msg -> failures.add(new ValidationFailure(msg, sourceMethod)));
      }
      if (!failures.isEmpty()) {
        return left(failures);
      }
      if (sourceMethods.isEmpty()) { // javapoet #739
        return left(Collections.singletonList(new ValidationFailure("expecting at least one abstract method", sourceElement)));
      }
      List<ExecutableElement> parametersMethods = sourceMethods.stream()
          .filter(m -> m.getAnnotation(Parameters.class) != null)
          .collect(Collectors.toList());
      if (parametersMethods.size() >= 2) {
        return left(Collections.singletonList(
            new ValidationFailure("duplicate @" + Parameters.class.getSimpleName()
                + " method", sourceMethods.get(1))));
      }
      return right(Methods.create(sourceMethods));
    });
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods(TypeMirror sourceElement) {
    List<ExecutableElement> acc = new ArrayList<>();
    Either<List<ValidationFailure>, TypeElement> element;
    while ((element = findRelevantMethods(sourceElement, acc)).isRight()) {
      sourceElement = element.orElse(null).getSuperclass();
    }
    if (!element.flip().orElse(Collections.emptyList()).isEmpty()) {
      return left(element.flip().orElse(Collections.emptyList()));
    }
    Map<Boolean, List<ExecutableElement>> map = acc.stream()
        .collect(Collectors.partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
    return right(AbstractMethods.create(map.get(true), map.get(false), types).unimplementedAbstract());
  }

  private Either<List<ValidationFailure>, TypeElement> findRelevantMethods(TypeMirror sourceElement, List<ExecutableElement> acc) {
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

  private Optional<String> validateSourceElement(TypeElement sourceElement) {
    Optional<String> maybeFailure = util.commonTypeChecks(sourceElement).map(s -> "command " + s);
    // the following *should* be done with Optional#or but we're currently limited to 1.8 API
    return Either.ofLeft(maybeFailure).orRight(Optional.<String>empty())
        .filter(nothing -> util.assertNoDuplicateAnnotations(sourceElement, Command.class, SuperCommand.class))
        .filter(nothing -> {
          List<? extends TypeMirror> interfaces = sourceElement.getInterfaces();
          if (!interfaces.isEmpty()) {
            return Optional.of("command cannot implement " + interfaces.get(0));
          }
          return Optional.empty();
        })
        .flip()
        .map(Optional::of)
        .orElseGet(Function.identity());
  }

  private static ClassName generatedClass(TypeElement sourceElement) {
    String name = String.join("_", ClassName.get(sourceElement).simpleNames()) + "_Parser";
    return ClassName.get(sourceElement).topLevelClassName().peerClass(name);
  }

  private Optional<String> validateParameterMethod(
      TypeElement sourceElement,
      ExecutableElement sourceMethod) {
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
    if (sourceElement.getAnnotation(SuperCommand.class) != null &&
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

  private void handleUnknownError(TypeElement sourceType, Throwable e) {
    String message = String.format("Unexpected error while processing %s: %s", sourceType, e.getMessage());
    e.printStackTrace(System.err);
    messager.printMessage(Diagnostic.Kind.ERROR, message, sourceType);
  }
}
