package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CollectorClassValidator extends SuppliedClassValidator {

  CollectorClassValidator(ExecutableElement sourceMethod) {
    super(sourceMethod);
  }

  TypeMirror findInputType(TypeElement supplierClass) {
    commonChecks(supplierClass, "collector");
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror suppliedType = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(this::boom);
    Map<String, TypeMirror> collectorTypeargs = resolveCollectorTypeargs(suppliedType);
    TypeTool tool = TypeTool.get();
    Optional<Map<String, TypeMirror>> solution = tool.unify(sourceMethod.getReturnType(), collectorTypeargs.get("R"));
    if (!solution.isPresent()) {
      throw boom();
    }
    return tool.substitute(collectorTypeargs.get("T"), solution.get());
  }

  private Map<String, TypeMirror> resolveCollectorTypeargs(TypeMirror suppliedType) {
    Map<String, TypeMirror> collectorTypeargs = Resolver.resolve("java.util.stream.Collector", suppliedType, "T", "A", "R");
    TypeTool tool = TypeTool.get();
    TypeMirror t = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(this::boom);
    TypeMirror r = Optional.ofNullable(collectorTypeargs.get("R")).orElseThrow(this::boom);
    Map<String, TypeMirror> solution = tool.unify(sourceMethod.getReturnType(), r).orElseThrow(this::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", tool.substitute(t, solution));
    resolved.put("R", sourceMethod.getReturnType());
    return resolved;
  }

  private ValidationException boom() {
    return ValidationException.create(sourceMethod, "There is a problem with the collector class.");
  }
}
