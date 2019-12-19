package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypevarMapping;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.function.Function;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
import static net.jbock.coerce.reference.ExpectedType.MAPPER;

public final class MapperClassValidator {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  public MapperClassValidator(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  public Either<String, ReferenceMapperType> checkReturnType() {
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass).getReferencedType();
    TypeMirror t = functionType.typeArguments().get(0);
    TypeMirror r = functionType.typeArguments().get(1);
    return tool().unify(tool().asType(String.class), t).flatMap(MAPPER::boom, t_result ->
        handle(functionType, r, t_result));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypeMirror r, TypevarMapping t_result) {
    return tool().unify(expectedReturnType, r).flatMap(MAPPER::boom, r_result ->
        handle(functionType, t_result, r_result));
  }

  private Either<String, ReferenceMapperType> handle(ReferencedType<Function> functionType, TypevarMapping t_result, TypevarMapping r_result) {
    return new Flattener(basicInfo, mapperClass)
        .getTypeParameters(Arrays.asList(t_result, r_result))
        .map(MAPPER::boom, typeParameters ->
            MapperType.create(tool(), functionType.isSupplier(), mapperClass, typeParameters));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
