package net.jbock.coerce.collectorabsent.explicit;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.collectorabsent.MapperAttempt;
import net.jbock.coerce.either.Either;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class ExplicitAttempt extends MapperAttempt {

  private final TypeElement mapperClass;

  ExplicitAttempt(TypeMirror expectedReturnType, CodeBlock extractExpr, ParameterSpec constructorParam, ParameterStyle style, TypeElement mapperClass) {
    super(expectedReturnType, extractExpr, constructorParam, style);
    this.mapperClass = mapperClass;
  }

  @Override
  public Either<String, Coercion> findCoercion(BasicInfo basicInfo) {
    return new MapperClassValidator(basicInfo::failure, basicInfo.tool(), getTestType(), mapperClass).checkReturnType()
        .map(Function.identity(), mapperType -> getCoercion(basicInfo, mapperType));
  }
}
