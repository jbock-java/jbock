package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
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
    MapSolution mapSolution = resolveFunctionTypeargs(functionClass, expectedReturnType);
    checkConstraints(mapperClass, functionClass);
    Optional<TypeMirror> mapperType = tool.substitute(mapperClass.asType(), mapSolution.solution);
    if (!mapperType.isPresent()) {
      throw boom("Invalid bounds");
    }
    return mapperType.get();
  }

  private static void checkConstraints(TypeElement root, TypeMirror target) {
    List<? extends TypeParameterElement> typeParameters = root.getTypeParameters();
  }

  private static MapSolution resolveFunctionTypeargs(
      TypeMirror functionType,
      TypeMirror expectedReturnType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve(tool.declared(Function.class), functionType, "T", "R");
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(() ->
        boom("The mapper must supply a Function."));
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(() ->
        boom("The mapper must supply a Function."));
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(() ->
        boom("The supplied function must take a String argument."));
    Optional<TypeMirror> returnType = tool.substitute(r, solution);
    if (returnType.isPresent()) {
      if (!tool.eql(returnType.get(), expectedReturnType)) {
        throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, returnType.get()));
      }
      return new MapSolution(returnType.get(), solution);
    }
    Map<String, TypeMirror> solution2 = tool.unify(expectedReturnType, r).orElseThrow(() ->
        boom("The supplied function must return x."));
    Optional<TypeMirror> returnType2 = tool.substitute(r, solution2);
    if (!returnType2.isPresent()) {
      throw boom("Invalid bounds");
    }
    return new MapSolution(returnType2.get(), solution2);
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
