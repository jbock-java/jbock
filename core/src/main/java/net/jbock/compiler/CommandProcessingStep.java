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
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.either.Either;
import net.jbock.qualifier.OptionType;
import net.jbock.qualifier.SourceElement;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                .sourceElement(new SourceElement(sourceElement))
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
      ImmutableList.Builder<ConvertedParameter<PositionalParameter>> positionalsBuilder =
          ImmutableList.builder();
      List<ValidationFailure> failures = new ArrayList<>();
      List<SourceMethod> positionalParameters = methods.params();
      for (SourceMethod sourceMethod : positionalParameters) {
        DaggerParameterComponent.builder()
            .module(module)
            .sourceMethod(sourceMethod)
            .alreadyCreatedParams(positionalsBuilder.build())
            .alreadyCreatedOptions(ImmutableList.of())
            .build()
            .positionalParameterFactory()
            .createPositionalParam(sourceMethod.index().orElse(positionalParameters.size() - 1))
            .accept(failures::add, positionalsBuilder::add);
      }
      if (flavour.isSuperCommand() && positionalParameters.isEmpty()) {
        failures.add(new ValidationFailure("in a @" + SuperCommand.class.getSimpleName() +
            ", at least one @" + Parameter.class.getSimpleName() + " must be defined", sourceElement));
      }
      ImmutableList<ConvertedParameter<PositionalParameter>> positionalParams = positionalsBuilder.build();
      failures.addAll(validatePositions(positionalParams));
      ImmutableList.Builder<ConvertedParameter<NamedOption>> optionsBuilder = ImmutableList.builder();
      for (SourceMethod sourceMethod : methods.options()) {
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
      ImmutableList<ConvertedParameter<NamedOption>> namedOptions = optionsBuilder.build();
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
    Set<String> keys = new HashSet<>();
    for (ConvertedParameter<? extends AbstractParameter> c : abstractParameters) {
      AbstractParameter p = c.parameter();
      String key = p.descriptionKey().orElse("");
      if (key.isEmpty()) {
        continue;
      }
      if (!keys.add(key)) {
        String message = "duplicate description key: " + key;
        failures.add(new ValidationFailure(message, p.sourceMethod()));
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
        result.add(new ValidationFailure(message, p.sourceMethod()));
      }
    }
    return result;
  }

  private Either<List<ValidationFailure>, Methods> createMethods(TypeElement sourceElement) {
    return findRelevantMethods(sourceElement.asType()).flatMap(sourceMethods -> {
      List<ValidationFailure> failures = new ArrayList<>();
      for (ExecutableElement sourceMethod : sourceMethods) {
        validateParameterMethod(sourceElement, sourceMethod)
            .map(msg -> new ValidationFailure(msg, sourceMethod))
            .ifPresent(failures::add);
      }
      if (sourceMethods.isEmpty()) { // javapoet #739
        failures.add(new ValidationFailure("expecting at least one abstract method", sourceElement));
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
      ExecutableElement method = sourceMethods.get(1).method();
      return Optional.of(new ValidationFailure(message, method));
    }
    return Optional.empty();
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
