package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

class CollectorClassValidator {

  static CollectorInfo getCollectorInfo(TypeMirror returnType, TypeElement collectorClass) throws TmpException {
    commonChecks(collectorClass, "collector");
    TypeTool tool = TypeTool.get();
    TypeMirror collectorType = getCollectorType(collectorClass);
    TypeMirror t = asDeclared(collectorType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(collectorType).getTypeArguments().get(2);
    Optional<Map<String, TypeMirror>> maybeSolution = tool.unify(returnType, r);
    if (!maybeSolution.isPresent()) {
      throw boom(String.format("The collector should return %s but returns %s", returnType, r));
    }
    Map<String, TypeMirror> solution = maybeSolution.get();
    Optional<TypeMirror> collectorClassSolved = tool.substitute(collectorClass.asType(), solution);
    if (!collectorClassSolved.isPresent()) {
      throw boom("Invalid bounds");
    }
    return CollectorInfo.create(tool.substitute(t, solution).orElse(t), collectorClassSolved.get());
  }

  private static TypeMirror getCollectorType(TypeElement collectorClass) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Supplier.class), collectorClass.asType());
    TypeMirror typeMirror = resolver.resolveTypevars().orElseThrow(() -> boom("not a Supplier"));
    if (tool.eql(typeMirror, tool.erasure(typeMirror))) {
      throw boom("the supplier must be parameterized");
    }
    TypeMirror collectorType = asDeclared(typeMirror).getTypeArguments().get(0);
    if (!tool.eql(tool.erasure(collectorType), tool.declared(Collector.class))) {
      throw boom("the supplier must supply a Collector");
    }
    if (tool.eql(collectorType, tool.erasure(collectorType))) {
      throw boom("the collector type must be parameterized");
    }
    return collectorType;
  }

  private static TmpException boom(String message) {
    return TmpException.create(String.format("There is a problem with the collector class: %s.", message));
  }
}
