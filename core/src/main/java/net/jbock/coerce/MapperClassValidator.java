package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

final class MapperClassValidator {

  static void checkReturnType(
      TypeElement mapperClass,
      TypeMirror expectedReturnType) throws TmpException {
    TypeTool tool = TypeTool.get();
    checkReturnType(mapperClass, expectedReturnType, tool);
  }

  static void checkReturnType(
      TypeElement mapperClass,
      TypeMirror expectedReturnType,
      TypeTool tool) throws TmpException {
    commonChecks(mapperClass, "mapper");
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw boom("The mapper class may not have type parameters");
    }
    TypeMirror functionType = getFunctionType(mapperClass, tool);
    TypeMirror string = tool.asType(String.class);
    TypeMirror t = asDeclared(functionType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(functionType).getTypeArguments().get(1);
    if (!tool.unify(string, t).isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    if (!tool.unify(expectedReturnType, r).isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
  }

  private static TypeMirror getFunctionType(TypeElement mapperClass, TypeTool tool) throws TmpException {
    Resolver resolver = Resolver.resolve(tool.asType(Supplier.class), mapperClass.asType(), tool);
    TypeMirror typeMirror = resolver.resolveTypevars().orElseThrow(() -> boom("not a Supplier"));
    if (tool.isRawType(typeMirror)) {
      throw boom("the supplier must be parameterized");
    }
    TypeMirror functionType = asDeclared(typeMirror).getTypeArguments().get(0);
    if (!tool.isSameErasure(functionType, Function.class)) {
      functionType = resolveFunctionType(functionType);
    }
    if (tool.isRawType(functionType)) {
      throw boom("the function type must be parameterized");
    }
    return functionType;
  }

  private static TypeMirror resolveFunctionType(TypeMirror functionType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.asType(Function.class), functionType, tool);
    return resolver.resolveTypevars().orElseThrow(() -> boom("The supplier must supply a Function"));
  }

  private static TmpException boom(String message) {
    return TmpException.create(String.format("There is a problem with the mapper class: %s.", message));
  }
}
