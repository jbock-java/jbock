package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.either.Either;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;
import java.util.stream.Collectors;

class MapperAttempt {

  private final CodeBlock extractExpr;
  private final ParameterSpec constructorParam;
  private final ParameterStyle style;
  private final TypeMirror testType;
  private final TypeElement mapperClass;

  MapperAttempt(TypeMirror testType, CodeBlock extractExpr, ParameterSpec constructorParam, ParameterStyle style, TypeElement mapperClass) {
    this.testType = testType;
    this.extractExpr = extractExpr;
    this.constructorParam = constructorParam;
    this.style = style;
    this.mapperClass = mapperClass;
  }

  private CodeBlock autoCollectExpr(BasicInfo basicInfo) {
    return autoCollectExpr(basicInfo, style);
  }

  static CodeBlock autoCollectExpr(BasicInfo basicInfo, ParameterStyle style) {
    switch (style) {
      case OPTIONAL:
        return CodeBlock.of(".findAny()");
      case REQUIRED:
        return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", basicInfo.optionType(),
            basicInfo.parameterName().enumConstant());
      case REPEATABLE:
        return CodeBlock.of(".collect($T.toList())", Collectors.class);
      default:
        throw new AssertionError("unexpected: " + style); // flags were handled earlier
    }
  }

  TypeMirror getTestType() {
    return testType;
  }

  Either<String, Coercion> findCoercion(BasicInfo basicInfo) {
    return new MapperClassValidator(basicInfo::failure, basicInfo.tool(), getTestType(), mapperClass).checkReturnType()
        .map(Function.identity(), mapperType ->
            Coercion.getCoercion(basicInfo, autoCollectExpr(basicInfo), mapperType, extractExpr, style, constructorParam));
  }
}
