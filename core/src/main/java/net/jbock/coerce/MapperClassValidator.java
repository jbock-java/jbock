package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

final class MapperClassValidator {

  static TypeMirror checkReturnType(TypeElement mapperClass, TypeMirror expectedReturnType) throws TmpException {
    commonChecks(mapperClass, "mapper");
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve(tool.declared(Supplier.class), mapperClass.asType(), "T");
    TypeMirror functionClass = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    MapSolution mapSolution = resolveFunctionTypeargs(functionClass);
    if (!tool.eql(mapSolution.returnType, expectedReturnType)) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, mapSolution.returnType));
    }
    Optional<TypeMirror> mapperType = tool.substitute(mapperClass.asType(), mapSolution.solution);
    if (!mapperType.isPresent()) {
      throw boom();
    }
    return mapperType.get();
  }

  private static MapSolution resolveFunctionTypeargs(TypeMirror functionType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve(tool.declared(Function.class), functionType, "T", "R");
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(MapperClassValidator::boom);
    Optional<TypeMirror> returnType = tool.substitute(r, solution);
    if (!returnType.isPresent()) {
      throw boom();
    }
    return new MapSolution(returnType.get(), solution);
  }

  private static class MapSolution {

    final TypeMirror returnType;
    final Map<String, TypeMirror> solution;

    MapSolution(TypeMirror returnType, Map<String, TypeMirror> solution) {
      this.returnType = returnType;
      this.solution = solution;
    }
  }

  private static TmpException boom() {
    return TmpException.create("There is a problem with the mapper class.");
  }

  private static TmpException boom(String message) {
    return TmpException.create("There is a problem with the mapper class: " + message);
  }
}
