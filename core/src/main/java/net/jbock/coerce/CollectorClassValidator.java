package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CollectorClassValidator {

  private final ExecutableElement sourceMethod;

  CollectorClassValidator(ExecutableElement sourceMethod) {
    this.sourceMethod = sourceMethod;
  }

  TypeMirror findInput(TypeElement supplierClass, TypeMirror returnType) {
    MapperClassValidator.commonChecks(sourceMethod, supplierClass, "collector");
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror suppliedType = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(this::boom);
    Map<String, TypeMirror> collectorTypeargs = resolveCollectorTypeargs(suppliedType, returnType);
    TypeTool tool = TypeTool.get();
    Optional<Map<String, TypeMirror>> solution = tool.unify(returnType, collectorTypeargs.get("R"));
    if (!solution.isPresent()) {
      throw boom();
    }
    return tool.substitute(collectorTypeargs.get("T"), solution.get());
  }

  private Map<String, TypeMirror> resolveCollectorTypeargs(
      TypeMirror suppliedType,
      TypeMirror returnType) {
    Map<String, TypeMirror> collectorTypeargs = Resolver.resolve("java.util.stream.Collector", suppliedType, "T", "A", "R");
    TypeTool tool = TypeTool.get();
    TypeMirror t = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(this::boom);
    TypeMirror r = Optional.ofNullable(collectorTypeargs.get("R")).orElseThrow(this::boom);
    Map<String, TypeMirror> solution = tool.unify(returnType, r).orElseThrow(this::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", tool.substitute(t, solution));
    resolved.put("R", returnType);
    return resolved;
  }

  private ValidationException boom() {
    return ValidationException.create(sourceMethod, "There is a problem with the collector class.");
  }
}
