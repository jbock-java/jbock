package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<Parameter> parameters;

  private final List<PositionalParameter> params;

  private final List<NamedOption> options;

  private final ParserFlavour flavour;

  private final GeneratedTypes generatedTypes;

  @Inject
  Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<NamedOption> namedOptions,
      List<PositionalParameter> params,
      ParserFlavour flavour,
      GeneratedTypes generatedTypes) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.params = params;
    this.options = namedOptions;
    this.flavour = flavour;
    this.parameters = ImmutableList.<Parameter>builder().addAll(params).addAll(options).build();
    this.generatedTypes = generatedTypes;
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream().filter(ALLOWED_MODIFIERS::contains).toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<Parameter> parameters() {
    return parameters;
  }

  public List<PositionalParameter> params() {
    return params;
  }

  public List<NamedOption> options() {
    return options;
  }

  public boolean isHelpParameterEnabled() {
    return !flavour.helpDisabled(sourceElement);
  }

  public String programName() {
    return flavour.programName(sourceElement);
  }

  public FieldSpec runBeforeExit() {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(Consumer.class), generatedTypes.parseResultType());
    return FieldSpec.builder(consumer, "runBeforeExit")
        .addModifiers(PRIVATE)
        .initializer("r -> {}")
        .build();
  }

  public boolean isSuperCommand() {
    return flavour.isSuperCommand();
  }
}
