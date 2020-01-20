package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.Skew.OPTIONAL;
import static net.jbock.coerce.Skew.REPEATABLE;
import static net.jbock.coerce.Skew.REQUIRED;

public class CollectorAbsentAuto {

  private final BasicInfo basicInfo;

  public CollectorAbsentAuto(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public Coercion findCoercion() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> listWrapped = tool().unwrap(List.class, returnType);
    if (opt.isPresent()) {
      Optionalish optional = opt.get();
      // optional match
      ParameterSpec param = basicInfo.constructorParam(optional.liftedType());
      return createCoercion(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL);
    }
    if (listWrapped.isPresent()) {
      // repeatable match
      ParameterSpec param = basicInfo.constructorParam(returnType);
      return createCoercion(listWrapped.get(), param, REPEATABLE);
    }
    // exact match (-> required)
    ParameterSpec param = basicInfo.constructorParam(returnType);
    return createCoercion(tool().box(returnType), param, REQUIRED);
  }

  private Coercion createCoercion(TypeMirror testType, ParameterSpec constructorParam, Skew skew) {
    return createCoercion(testType, CodeBlock.of("$N", constructorParam), constructorParam, skew);
  }

  private Coercion createCoercion(TypeMirror testType, CodeBlock extractExpr, ParameterSpec constructorParam, Skew skew) {
    return basicInfo.findAutoMapper(testType)
        .map(mapExpr -> Coercion.getCoercion(basicInfo, MapperAttempt.autoCollectExpr(basicInfo, skew), mapExpr, extractExpr, skew, constructorParam))
        .orElseThrow(() -> basicInfo.failure(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            basicInfo.originalReturnType())));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
