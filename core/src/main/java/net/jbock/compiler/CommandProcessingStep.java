package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;
import net.jbock.coerce.Util;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.view.GeneratedClass;
import net.jbock.either.Either;

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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class CommandProcessingStep implements BasicAnnotationProcessor.Step {

  private final TypeTool tool;
  private final Messager messager;
  private final Filer filer;
  private final Elements elements;

  @Inject
  CommandProcessingStep(TypeTool tool, Messager messager, Filer filer, Elements elements) {
    this.tool = tool;
    this.messager = messager;
    this.filer = filer;
    this.elements = elements;
  }

  @Component(modules = ParameterModule.class)
  interface ParameterComponent {

    PositionalParamFactory positionalParameterFactory();

    NamedOptionFactory namedOptionFactory();

    @Component.Builder
    interface Builder {

      @BindsInstance
      Builder sourceMethod(ExecutableElement sourceMethod);

      @BindsInstance
      Builder typeTool(TypeTool tool);

      @BindsInstance
      Builder optionType(ClassName optionType);

      @BindsInstance
      Builder description(String[] description);

      @BindsInstance
      Builder alreadyCreated(ImmutableList<Parameter> alreadyCreated);

      Builder parameterModule(ParameterModule module);

      ParameterComponent build();
    }
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Command.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    for (TypeElement typeElement : ElementFilter.typesIn(elementsByAnnotation.values())) {
      processSourceElement(typeElement);
    }
    return Collections.emptySet();
  }

  private void processSourceElement(TypeElement sourceElement) {
    ClassName generatedClass = generatedClass(sourceElement);
    try {
      validateSourceElement(sourceElement).ifPresent(msg -> {
        throw ValidationException.create(sourceElement, msg);
      });
      ClassName optionType = generatedClass.nestedClass("Option");
      Either<List<ValidationFailure>, List<Parameter>> either = getParams(sourceElement, optionType);
      either.ifPresentOrElse(parameters -> {
        Context context = new Context(sourceElement, generatedClass, optionType, parameters);
        TypeSpec typeSpec = GeneratedClass.create(context).define();
        write(sourceElement, context.generatedClass(), typeSpec);
      }, failures -> {
        for (ValidationFailure failure : failures) {
          messager.printMessage(Diagnostic.Kind.ERROR, failure.message(), failure.about());
        }
      });
    } catch (ValidationException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.about);
    } catch (AssertionError error) {
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
        writer.write(sourceCode);
      } catch (IOException e) {
        handleUnknownError(sourceElement, e);
      }
    } catch (IOException e) {
      handleUnknownError(sourceElement, e);
    }
  }

  private Either<List<ValidationFailure>, List<Parameter>> getParams(TypeElement sourceElement, ClassName optionType) {
    return createMethods(sourceElement).select(methods -> {
      List<Parameter> params = new ArrayList<>();
      AnnotationUtil annotationUtil = new AnnotationUtil();
      List<ValidationFailure> failures = new ArrayList<>();
      List<ExecutableElement> positionalParameters = methods.params();
      for (int i = 0, executableElementsSize = positionalParameters.size(); i < executableElementsSize; i++) {
        ExecutableElement sourceMethod = positionalParameters.get(i);
        Optional<TypeElement> mapperClass = annotationUtil.getMapper(sourceMethod);
        Param param = sourceMethod.getAnnotation(Param.class);
        ParameterModule module = new ParameterModule(sourceElement, mapperClass, param.bundleKey());
        ParameterComponent.Builder builder = DaggerCommandProcessingStep_ParameterComponent.builder()
            .optionType(optionType)
            .sourceMethod(sourceMethod)
            .typeTool(tool)
            .alreadyCreated(ImmutableList.copyOf(params))
            .parameterModule(module)
            .description(getDescription(sourceMethod));
        builder.build().positionalParameterFactory().createPositionalParam(i)
            .ifPresentOrElse(params::add, failures::add);
      }
      boolean anyMnemonics = methods.options().stream().anyMatch(method -> method.getAnnotation(Option.class).mnemonic() != ' ');
      for (ExecutableElement sourceMethod : methods.options()) {
        Optional<TypeElement> mapperClass = annotationUtil.getMapper(sourceMethod);
        ParameterModule module = new ParameterModule(sourceElement, mapperClass, sourceMethod.getAnnotation(Option.class).bundleKey());
        ParameterComponent.Builder builder = DaggerCommandProcessingStep_ParameterComponent.builder()
            .optionType(optionType)
            .sourceMethod(sourceMethod)
            .typeTool(tool)
            .alreadyCreated(ImmutableList.copyOf(params))
            .parameterModule(module)
            .description(getDescription(sourceMethod));
        builder.build().namedOptionFactory().createNamedOption(anyMnemonics)
            .ifPresentOrElse(params::add, failures::add);
      }
      return failures.isEmpty() ? right(params) : left(failures);
    });
  }

  private Either<List<ValidationFailure>, Methods> createMethods(TypeElement sourceElement) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ExecutableElement> sourceMethods = methodsIn(sourceElement.getEnclosedElements())
        .stream()
        .filter(sourceMethod -> sourceMethod.getModifiers().contains(ABSTRACT))
        .collect(Collectors.toList());
    for (ExecutableElement sourceMethod : sourceMethods) {
      validateParameterMethod(sourceMethod)
          .ifPresent(msg -> failures.add(new ValidationFailure(msg, sourceMethod)));
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    if (sourceMethods.isEmpty()) { // javapoet #739
      return left(Collections.singletonList(new ValidationFailure("expecting at least one abstract method", sourceElement)));
    }
    return right(Methods.create(sourceMethods));
  }

  private Optional<String> validateSourceElement(TypeElement sourceElement) {
    Optional<String> maybeFailure = commonChecks(sourceElement).map(s -> "command " + s);
    return Either.<String, Void>fromOptionalFailure(maybeFailure)
        .filter(nothing -> {
          List<? extends TypeMirror> interfaces = sourceElement.getInterfaces();
          if (!interfaces.isEmpty()) {
            return Optional.of("command cannot implement " + interfaces.get(0));
          }
          return Optional.empty();
        })
        .filter(nothing -> {
          TypeMirror superclass = sourceElement.getSuperclass();
          boolean isObject = tool.isSameType(superclass, Object.class.getCanonicalName());
          if (!isObject) {
            return Optional.of("command cannot inherit from " + superclass);
          }
          return Optional.empty();
        })
        .swap()
        .map(Optional::of)
        .orRecover(nothing -> Optional.empty());
  }

  private static ClassName generatedClass(TypeElement sourceElement) {
    String name = String.join("_", ClassName.get(sourceElement).simpleNames()) + "_Parser";
    return ClassName.get(sourceElement).topLevelClassName().peerClass(name);
  }

  private static Optional<String> validateParameterMethod(ExecutableElement sourceMethod) {
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
    if (sourceMethod.getAnnotation(Param.class) == null && sourceMethod.getAnnotation(Option.class) == null) {
      return Optional.of(String.format("add @%s or @%s annotation",
          Option.class.getSimpleName(), Param.class.getSimpleName()));
    }
    if (sourceMethod.getAnnotation(Param.class) != null && sourceMethod.getAnnotation(Option.class) != null) {
      return Optional.of(String.format("use @%s or @%s annotation but not both",
          Option.class.getSimpleName(), Param.class.getSimpleName()));
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

  private String[] getDescription(ExecutableElement method) {
    String docComment = elements.getDocComment(method);
    return docComment == null ? new String[0] : tokenizeJavadoc(docComment);
  }

  private static String[] tokenizeJavadoc(String docComment) {
    String[] tokens = docComment.trim().split("\\R", -1);
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      String token = t.trim();
      if (token.startsWith("@")) {
        return result.toArray(new String[0]);
      }
      if (!token.isEmpty()) {
        result.add(token);
      }
    }
    return result.toArray(new String[0]);
  }

  private void handleUnknownError(TypeElement sourceType, Throwable e) {
    String message = String.format("Unexpected error while processing %s: %s", sourceType, e.getMessage());
    e.printStackTrace(System.err);
    messager.printMessage(Diagnostic.Kind.ERROR, message, sourceType);
  }
}
