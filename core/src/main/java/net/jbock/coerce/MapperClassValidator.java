package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class MapperClassValidator extends SuppliedClassValidator {

  private final ExecutableElement sourceMethod;

  MapperClassValidator(ExecutableElement sourceMethod) {
    super(sourceMethod);
    this.sourceMethod = sourceMethod;
  }

  TypeMirror findReturnType(TypeElement supplierClass) {
    commonChecks(supplierClass, "mapper");
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror functionClass = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(this::boom);
    Map<String, TypeMirror> functionTypeargs = resolveFunctionTypeargs(functionClass);
    return functionTypeargs.get("R");
  }

  private Map<String, TypeMirror> resolveFunctionTypeargs(
      TypeMirror functionType) {
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve("java.util.function.Function", functionType, "T", "R");
    TypeTool tool = TypeTool.get();
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(this::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(this::boom);
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(this::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", string);
    resolved.put("R", tool.substitute(r, solution));
    return resolved;
  }

  private ValidationException boom() {
    return ValidationException.create(sourceMethod, "There is a problem with the mapper class.");
  }
}
