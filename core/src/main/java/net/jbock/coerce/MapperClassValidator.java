package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

final class MapperClassValidator {

  static TypeMirror findReturnType(TypeElement supplierClass, TypeMirror goal) throws TmpException {
    commonChecks(supplierClass, "mapper");
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror functionClass = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> functionTypeargs = resolveFunctionTypeargs(functionClass);
    TypeMirror returnType = functionTypeargs.get("R");
    if (!TypeTool.get().equals(returnType, goal)) {
      throw boom();
    }
    return returnType;
  }

  private static Map<String, TypeMirror> resolveFunctionTypeargs(
      TypeMirror functionType) throws TmpException {
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve("java.util.function.Function", functionType, "T", "R");
    TypeTool tool = TypeTool.get();
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", string);
    resolved.put("R", tool.substitute(r, solution));
    return resolved;
  }

  private static TmpException boom() {
    return TmpException.create("There is a problem with the mapper class.");
  }
}
