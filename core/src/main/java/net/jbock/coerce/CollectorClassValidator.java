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

  static class CollectorResult {
    final TypeMirror inputType;
    final TypeMirror collectorType;

    CollectorResult(TypeMirror inputType, TypeMirror collectorType) {
      this.inputType = inputType;
      this.collectorType = collectorType;
    }
  }

  static CollectorResult findInputType(TypeMirror returnType, TypeElement collectorClass) throws TmpException {
    commonChecks(collectorClass, "collector");
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> collectorTypeargs = Resolver.resolve(tool.declared(Supplier.class), collectorClass.asType(), "T");
    TypeMirror collectorTypeWithTypeargs = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    CollectSolution solution = resolveCollectorTypeargs(returnType, collectorTypeWithTypeargs);
    Optional<TypeMirror> collectorType = TypeTool.get().substituteFlat(collectorClass.asType(), solution.solution);
    if (!collectorType.isPresent()) {
      throw boom();
    }
    return new CollectorResult(solution.inputType, collectorType.get());
  }

  private static CollectSolution resolveCollectorTypeargs(TypeMirror returnType, TypeMirror collectorType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> collectorTypeargs = Resolver.resolve(tool.declared(Collector.class), collectorType, "T", "A", "R");
    TypeMirror t = Optional.ofNullable(collectorTypeargs.get("T")).orElseThrow(CollectorClassValidator::boom);
    TypeMirror r = Optional.ofNullable(collectorTypeargs.get("R")).orElseThrow(CollectorClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(returnType, r).orElseThrow(CollectorClassValidator::boom);
    return new CollectSolution(tool.substitute(t, solution), solution);
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
}
