package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.function.BiConsumer;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<Coercion<? extends Parameter>> parameters;

  private final List<Coercion<PositionalParameter>> params;

  private final boolean anyRepeatableParam;

  private final List<Coercion<NamedOption>> options;

  private final ParserFlavour flavour;

  private final GeneratedTypes generatedTypes;

  @Inject
  Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<Coercion<NamedOption>> namedOptions,
      List<Coercion<PositionalParameter>> params,
      ParserFlavour flavour,
      GeneratedTypes generatedTypes) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.params = params;
    this.options = namedOptions;
    this.flavour = flavour;
    this.parameters = ImmutableList.<Coercion<? extends Parameter>>builder().addAll(params).addAll(options).build();
    this.anyRepeatableParam = params.stream().anyMatch(Coercion::isRepeatable);
    this.generatedTypes = generatedTypes;
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream().filter(ALLOWED_MODIFIERS::contains).toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<Coercion<? extends Parameter>> parameters() {
    return parameters;
  }

  public List<Coercion<PositionalParameter>> params() {
    return params;
  }

  public List<Coercion<NamedOption>> options() {
    return options;
  }

  public boolean isHelpParameterEnabled() {
    return !flavour.helpDisabled(sourceElement);
  }

  public String programName() {
    return flavour.programName(sourceElement);
  }

  public FieldSpec exitHookField() {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(BiConsumer.class),
        generatedTypes.parseResultType(), ClassName.get(Integer.class));
    return FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer("(r, code) -> $T.exit(code)", System.class)
        .build();
  }

  public boolean isSuperCommand() {
    return flavour.isSuperCommand();
  }

  public boolean anyRepeatableParam() {
    return anyRepeatableParam;
  }

  public String getSuccessResultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }
}
