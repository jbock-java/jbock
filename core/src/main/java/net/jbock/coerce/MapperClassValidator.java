package net.jbock.coerce;

import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

public final class MapperClassValidator extends ParameterScoped {

  private final TypeElement mapperClass;
  private final ReferenceTool referenceTool;

  @Inject
  MapperClassValidator(
      ParameterContext parameterContext,
      TypeElement mapperClass,
      ReferenceTool referenceTool) {
    super(parameterContext);
    this.mapperClass = mapperClass;
    this.referenceTool = referenceTool;
  }

  private ValidationException mapperFailure(String message) {
    return ValidationException.create(sourceMethod(), String.format("There is a problem with the mapper class: %s.", message));
  }
}
