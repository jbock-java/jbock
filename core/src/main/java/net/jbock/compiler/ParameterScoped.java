package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.List;

public class ParameterScoped {

  private final ParameterContext parameterContext;

  public ParameterScoped(ParameterContext parameterContext) {
    this.parameterContext = parameterContext;
  }

  public final TypeElement sourceElement() {
    return parameterContext.sourceElement;
  }

  public final TypeTool tool() {
    return parameterContext.tool;
  }

  public final ClassName optionType() {
    return parameterContext.optionType;
  }

  final ImmutableList<ConvertedParameter<NamedOption>> alreadyCreatedOptions() {
    return parameterContext.alreadyCreatedOptions;
  }

  final ImmutableList<ConvertedParameter<PositionalParameter>> alreadyCreatedParams() {
    return parameterContext.alreadyCreatedParams;
  }

  final List<String> description() {
    return Arrays.asList(parameterContext.description.getValue());
  }

  public final EnumName enumName() {
    return parameterContext.enumName;
  }

  public final ParameterSpec constructorParam(TypeMirror constructorParamType) {
    return ParameterSpec.builder(TypeName.get(constructorParamType), enumName().camel()).build();
  }

  public final Types types() {
    return tool().types();
  }

  public ParameterContext parameterContext() {
    return parameterContext;
  }

  public ParserFlavour flavour() {
    return parameterContext.flavour;
  }
}
