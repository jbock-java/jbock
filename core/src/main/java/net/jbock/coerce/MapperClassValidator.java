package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;

final class MapperClassValidator {

  static TypeMirror checkReturnType(TypeElement mapperClass, TypeMirror expectedReturnType) throws TmpException {
    commonChecks(mapperClass, "mapper");
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw boom("The mapper class may not have type parameters");
    }
    TypeTool tool = TypeTool.get();
    TypeMirror functionType = getFunctionType(mapperClass);
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = tool.asDeclared(functionType).getTypeArguments().get(0);
    TypeMirror r = tool.asDeclared(functionType).getTypeArguments().get(1);
    if (!tool.unify(string, t).isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    if (!tool.unify(expectedReturnType, r).isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return mapperClass.asType();
  }

  private static TypeMirror getFunctionType(TypeElement mapperClass) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Supplier.class), mapperClass.asType(), "T");
    TypeMirror typeMirror = resolver.resolveTypevars().orElseThrow(() -> boom("not a Supplier"));
    if (tool.eql(typeMirror, tool.erasure(typeMirror))) {
      throw boom("the supplier must be parameterized");
    }
    TypeMirror functionType = tool.asDeclared(typeMirror).getTypeArguments().get(0);
    if (!tool.eql(tool.erasure(functionType), tool.declared(Function.class))) {
      functionType = resolveFunctionType(functionType);
    }
    if (tool.eql(functionType, tool.erasure(functionType))) {
      throw boom("the function type must be parameterized");
    }
    return functionType;
  }

  private static TypeMirror resolveFunctionType(TypeMirror functionType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Function.class), functionType, "T", "R");
    return resolver.resolveTypevars().orElseThrow(() -> boom("The supplier must supply a Function"));
  }

  private static TmpException boom(String message) {
    return TmpException.create(String.format("There is a problem with the mapper class: %s.", message));
  }
}
