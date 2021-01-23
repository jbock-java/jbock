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
import net.jbock.coerce.SuppliedClassValidator;
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
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.TypeTool.AS_DECLARED;

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
      validateSourceElement(sourceElement);
      ClassName optionType = generatedClass.nestedClass("Option");
      List<Parameter> parameters = getParams(sourceElement, optionType);
      if (parameters.isEmpty()) { // javapoet #739
        throw ValidationException.create(sourceElement, "Define at least one abstract method");
      }

      checkOnlyOnePositionalList(parameters);
      checkRankConsistentWithPosition(parameters);

      Context context = new Context(sourceElement, generatedClass, optionType, parameters);
      TypeSpec typeSpec = GeneratedClass.create(context).define();
      write(sourceElement, context.generatedClass(), typeSpec);
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

  private List<Parameter> getParams(TypeElement sourceElement, ClassName optionType) {
    Methods methods = Methods.create(methodsIn(sourceElement.getEnclosedElements()).stream()
        .filter(CommandProcessingStep::validateParameterMethod)
        .collect(Collectors.toList()));
    List<Parameter> params = new ArrayList<>();
    AnnotationUtil annotationUtil = new AnnotationUtil();
    for (int i = 0; i < methods.params().size(); i++) {
      ExecutableElement sourceMethod = methods.params().get(i);
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
      params.add(builder.build().positionalParameterFactory().createPositionalParam(i)
          .orElseThrow(s -> ValidationException.create(sourceMethod, s)));
    }
    boolean anyMnemonics = methods.options().stream().anyMatch(method -> method.getAnnotation(Option.class).mnemonic() != ' ');
    for (ExecutableElement option : methods.options()) {
      Optional<TypeElement> mapperClass = annotationUtil.getMapper(option);
      ParameterModule module = new ParameterModule(sourceElement, mapperClass, option.getAnnotation(Option.class).bundleKey());
      ParameterComponent.Builder builder = DaggerCommandProcessingStep_ParameterComponent.builder()
          .optionType(optionType)
          .sourceMethod(option)
          .typeTool(tool)
          .alreadyCreated(ImmutableList.copyOf(params))
          .parameterModule(module)
          .description(getDescription(option));
      params.add(builder.build().namedOptionFactory().createNamedOption(anyMnemonics)
          .orElseThrow(s -> ValidationException.create(option, s)));
    }
    if (!sourceElement.getAnnotation(Command.class).helpDisabled()) {
      methods.options().forEach(this::checkHelp);
    }
    return params;
  }

  private static boolean validateParameterMethod(ExecutableElement method) {
    if (!method.getModifiers().contains(ABSTRACT)) {
      if (method.getAnnotation(Param.class) != null || method.getAnnotation(Option.class) != null) {
        throw ValidationException.create(method, "The method must be abstract.");
      }
      return false;
    }
    if (!method.getParameters().isEmpty()) {
      throw ValidationException.create(method, "The method may not have any parameters.");
    }
    if (!method.getTypeParameters().isEmpty()) {
      throw ValidationException.create(method, "The method may not have any type parameters.");
    }
    if (!method.getThrownTypes().isEmpty()) {
      throw ValidationException.create(method, "The method may not declare any exceptions.");
    }
    if (method.getAnnotation(Param.class) == null && method.getAnnotation(Option.class) == null) {
      throw ValidationException.create(method, String.format("Annotate this method with either @%s or @%s",
          Option.class.getSimpleName(), Param.class.getSimpleName()));
    }
    if (method.getAnnotation(Param.class) != null && method.getAnnotation(Option.class) != null) {
      throw ValidationException.create(method, String.format("Use either @%s or @%s annotation, but not both",
          Option.class.getSimpleName(), Param.class.getSimpleName()));
    }
    if (isUnreachable(method.getReturnType())) {
      throw ValidationException.create(method, "Unreachable parameter type.");
    }
    return true;
  }

  private void validateSourceElement(TypeElement sourceElement) {
    Either.fromOptionalFailure(SuppliedClassValidator.commonChecks(sourceElement))
        .mapLeft(s -> "command " + s)
        .filter(s -> {
          List<? extends TypeMirror> interfaces = sourceElement.getInterfaces();
          if (!interfaces.isEmpty()) {
            return Optional.of("command cannot implement " + interfaces.get(0));
          }
          return Optional.empty();
        })
        .filter(s -> {
          TypeMirror superclass = sourceElement.getSuperclass();
          boolean isObject = tool.isSameType(superclass, Object.class.getCanonicalName());
          if (!isObject) {
            return Optional.of("command cannot inherit from " + superclass);
          }
          return Optional.empty();
        })
        .orElseThrow(message -> ValidationException.create(sourceElement, message));
  }

  private static ClassName generatedClass(TypeElement sourceElement) {
    String name = String.join("_", ClassName.get(sourceElement).simpleNames()) + "_Parser";
    return ClassName.get(sourceElement).topLevelClassName().peerClass(name);
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

  private void checkHelp(ExecutableElement option) {
    if ("help".equals(option.getAnnotation(Option.class).value())) {
      throw ValidationException.create(option, "'help' is reserved. Either disable the help feature or change the option name to something else.");
    }
  }

  private static void checkOnlyOnePositionalList(List<Parameter> allParams) {
    allParams.stream().filter(p -> p.isRepeatable() && p.isPositional())
        .skip(1).findAny().ifPresent(p -> {
      throw p.validationError("There can only be one repeatable param.");
    });
  }

  private static void checkRankConsistentWithPosition(List<Parameter> allParams) {
    int currentOrdinal = -1;
    for (Parameter param : allParams) {
      OptionalInt order = param.positionalOrder();
      if (!order.isPresent()) {
        continue;
      }
      if (order.getAsInt() < currentOrdinal) {
        throw param.validationError("Bad position, expecting Optional < Required < Repeatable");
      }
      currentOrdinal = order.getAsInt();
    }
  }

  private void handleUnknownError(TypeElement sourceType, Throwable e) {
    String message = String.format("JBOCK: Unexpected error while processing %s: %s", sourceType, e.getMessage());
    e.printStackTrace(System.err);
    messager.printMessage(Diagnostic.Kind.ERROR, message, sourceType);
  }
}
