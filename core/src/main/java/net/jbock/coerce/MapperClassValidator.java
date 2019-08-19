package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

final class MapperClassValidator {

  static void checkReturnType(
      TypeElement mapperClass,
      TypeMirror expectedReturnType,
      BasicInfo basicInfo) {
    commonChecks(basicInfo, mapperClass, "mapper");
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw boom(basicInfo, "The mapper class may not have type parameters");
    }
    TypeMirror functionType = getFunctionType(mapperClass, basicInfo);
    TypeMirror string = basicInfo.tool().asType(String.class);
    TypeMirror t = asDeclared(functionType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(functionType).getTypeArguments().get(1);
    if (!basicInfo.tool().unify(string, t).isPresent()) {
      throw boom(basicInfo, String.format("The supplied function must take a String argument, but takes %s", t));
    }
    if (!basicInfo.tool().unify(expectedReturnType, r).isPresent()) {
      throw boom(basicInfo, String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
  }

  private static TypeMirror getFunctionType(TypeElement mapperClass, BasicInfo basicInfo) {
    Resolver resolver = Resolver.resolve(basicInfo.tool().asType(Supplier.class), mapperClass.asType(), basicInfo.tool());
    TypeMirror typeMirror = resolver.resolveTypevars().orElseThrow(() -> boom(basicInfo, "not a Supplier"));
    if (basicInfo.tool().isRawType(typeMirror)) {
      throw boom(basicInfo, "the supplier must be parameterized");
    }
    TypeMirror functionType = asDeclared(typeMirror).getTypeArguments().get(0);
    if (!basicInfo.tool().isSameErasure(functionType, Function.class)) {
      functionType = resolveFunctionType(basicInfo, functionType);
    }
    if (basicInfo.tool().isRawType(functionType)) {
      throw boom(basicInfo, "the function type must be parameterized");
    }
    return functionType;
  }

  private static TypeMirror resolveFunctionType(BasicInfo basicInfo, TypeMirror functionType) {
    TypeTool tool = TypeTool.get();
    Resolver resolver = Resolver.resolve(tool.asType(Function.class), functionType, tool);
    return resolver.resolveTypevars().orElseThrow(() -> boom(basicInfo, "The supplier must supply a Function"));
  }

  private static ValidationException boom(BasicInfo basicInfo, String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }
}
