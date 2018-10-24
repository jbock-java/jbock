package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

class CollectorClassValidator {

  static CollectorInfo getCollectorInfo(TypeMirror returnType, TypeElement collectorClass) throws TmpException {
    commonChecks(collectorClass, "collector");
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Supplier.class), collectorClass.asType(), "T");
    Map<String, TypeMirror> collectorTypeargs = resolver.asMap();
    TypeMirror collectorTypeWithTypeargs = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    CollectSolution solution = resolveCollectorTypeargs(returnType, collectorTypeWithTypeargs);
    Optional<TypeMirror> collectorType = TypeTool.get().substitute(collectorClass.asType(), solution.solution);
    if (!collectorType.isPresent()) {
      throw boom();
    }
    return CollectorInfo.create(solution.inputType, collectorType.get());
  }

  private static CollectSolution resolveCollectorTypeargs(TypeMirror returnType, TypeMirror collectorType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Collector.class), collectorType, "T", "A", "R");
    Map<String, TypeMirror> collectorTypeargs = resolver.asMap();
    TypeMirror t = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    TypeMirror r = Optional.ofNullable(collectorTypeargs.get("R")).orElseThrow(CollectorClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(returnType, r).orElseThrow(() ->
        boom(returnType.toString() + " can't be unified with " + r));
    Optional<TypeMirror> inputType = tool.substitute(t, solution);
    if (!inputType.isPresent()) {
      throw boom();
    }
    return new CollectSolution(inputType.get(), solution);
  }

  private static class CollectSolution {

    final TypeMirror inputType;
    final Map<String, TypeMirror> solution;

    CollectSolution(TypeMirror inputType, Map<String, TypeMirror> solution) {
      this.inputType = inputType;
      this.solution = solution;
    }
  }

  private static TmpException boom() {
    return TmpException.create("There is a problem with the collector class.");
  }

  private static TmpException boom(String message) {
    return TmpException.create("There is a problem with the collector class: " + message);
  }
}
