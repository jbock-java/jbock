package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

class CollectorClassValidator {

  static TypeMirror findInputType(TypeMirror returnType, TypeElement supplierClass) throws TmpException {
    commonChecks(supplierClass, "collector");
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror suppliedType = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    Map<String, TypeMirror> collectorTypeargs = resolveCollectorTypeargs(returnType, suppliedType);
    return collectorTypeargs.get("T");
  }

  private static Map<String, TypeMirror> resolveCollectorTypeargs(TypeMirror returnType, TypeMirror collectorType) throws TmpException {
    Map<String, TypeMirror> collectorTypeargs = Resolver.resolve("java.util.stream.Collector", collectorType, "T", "A", "R");
    TypeTool tool = TypeTool.get();
    TypeMirror t = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    TypeMirror r = Optional.ofNullable(collectorTypeargs.get("R")).orElseThrow(CollectorClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(returnType, r).orElseThrow(CollectorClassValidator::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", tool.substitute(t, solution));
    resolved.put("R", returnType);
    return resolved;
  }

  private static TmpException boom() {
    return TmpException.findWarning("There is a problem with the collector class.");
  }
}
