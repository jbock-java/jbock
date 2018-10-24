package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Resolver resolver = Resolver.resolve(tool.declared(Supplier.class), mapperClass.asType(), "T");
    TypeMirror typeMirror = resolveTypevars(resolver);
    TypeMirror functionType = tool.asDeclared(typeMirror).getTypeArguments().get(0);
    if (!tool.eql(tool.erasure(functionType), tool.declared(Function.class))) {
      functionType = resolveFunctionType(functionType);
    }
    if (tool.eql(functionType, tool.erasure(functionType))) {
      throw boom("the function type must be parameterized");
    }
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

  private static TypeMirror resolveFunctionType(TypeMirror functionType) throws TmpException {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.declared(Function.class), functionType, "T", "R");
    if (resolver.extensions().isEmpty()) {
      throw boom("The mapper must supply a Function");
    }
    return resolveTypevars(resolver);
  }

  private static TypeMirror resolveTypevars(Resolver resolver) {
    List<Extension> extensions = resolver.extensions();
    TypeMirror x = extensions.get(0).extensionClass();
    for (int i = 1; i < extensions.size(); i++) {
      Extension extension = extensions.get(i);
      x = step(x, extension);
    }
    return x;
  }

  private static TypeMirror step(TypeMirror x, Extension ex1) {
    List<? extends TypeMirror> typeArguments = TypeTool.get().asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = ex1.baseClass().getTypeParameters();
    Map<String, TypeMirror> resolution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      resolution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    return TypeTool.get().substitute(ex1.extensionClass(), resolution).orElse(ex1.extensionClass());
  }

  private static TmpException boom(String message) {
    return TmpException.create(String.format("There is a problem with the mapper class: %s.", message));
  }
}
