package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

class ParameterFactory {

  final ParameterContext parameterContext;

  ParameterFactory(ParameterContext parameterContext) {
    this.parameterContext = parameterContext;
  }

  ExecutableElement sourceMethod() {
    return parameterContext.sourceMethod;
  }

  TypeElement sourceElement() {
    return parameterContext.sourceElement;
  }

  TypeTool tool() {
    return parameterContext.tool;
  }

  ClassName optionType() {
    return parameterContext.optionType;
  }

  ImmutableList<Parameter> alreadyCreated() {
    return parameterContext.alreadyCreated;
  }

  String[] description() {
    return parameterContext.description;
  }

  Optional<TypeElement> mapperClass() {
    return parameterContext.mapperClass;
  }

  String bundleKey() {
    return parameterContext.bundleKey;
  }

  EnumName enumName() {
    return parameterContext.enumName;
  }
}
