package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

final class MapperClassValidator {

  private final BasicInfo basicInfo;

  MapperClassValidator(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  MapperType checkReturnType(
      TypeElement mapperClass,
      TypeMirror expectedReturnType) {
    commonChecks(basicInfo, mapperClass, "mapper");
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw boom("The mapper class may not have type parameters");
    }
    MapperType mapperType = getMapperType(mapperClass);
    TypeMirror string = tool().asType(String.class);
    TypeMirror t = asDeclared(mapperType.type()).getTypeArguments().get(0);
    TypeMirror r = asDeclared(mapperType.type()).getTypeArguments().get(1);
    if (!tool().unify(string, t).isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    if (!tool().unify(expectedReturnType, r).isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return mapperType;
  }

  private MapperType getMapperType(TypeElement mapperClass) {
    Optional<TypeMirror> supplier = Resolver.resolve(
        Supplier.class,
        mapperClass.asType(),
        basicInfo.tool()).resolveTypevars();
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return MapperType.create(basicInfo, typeArgs.get(0), true, mapperClass);
    }
    TypeMirror mapper = Resolver.resolve(
        Function.class,
        mapperClass.asType(),
        basicInfo.tool()).resolveTypevars().orElseThrow(() ->
        boom("not a Function or Supplier<Function>"));
    return MapperType.create(basicInfo, mapper, false, mapperClass);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }
}
