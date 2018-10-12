package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

final class MapperClassValidator {

  static void checkReturnType(TypeElement supplierClass, TypeMirror expectedReturnType) throws TmpException {
    commonChecks(supplierClass, "mapper");
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve(tool.declared(Supplier.class), supplierClass.asType(), "T");
    TypeMirror functionClass = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> functionTypeargs = resolveFunctionTypeargs(functionClass);
    TypeMirror returnType = functionTypeargs.get("R");
    if (!TypeTool.get().eql(returnType, expectedReturnType)) {
      throw boom();
    }
  }

  private static Map<String, TypeMirror> resolveFunctionTypeargs(TypeMirror functionType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve(tool.declared(Function.class), functionType, "T", "R");
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(MapperClassValidator::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(MapperClassValidator::boom);
    Map<String, TypeMirror> result = new HashMap<>();
    result.put("T", tool.substitute(t, solution));
    result.put("R", tool.substitute(r, solution));
    return result;
  }

  private static TmpException boom() {
    return TmpException.create("There is a problem with the mapper class.");
  }
}
