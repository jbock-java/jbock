package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.ParameterStyle.OPTIONAL;
import static net.jbock.coerce.ParameterStyle.REPEATABLE;
import static net.jbock.coerce.ParameterStyle.REQUIRED;

public class CollectorAbsentAuto {

  private final BasicInfo basicInfo;

  public CollectorAbsentAuto(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  private MapperAttempt getAttempt() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> listWrapped = tool().unwrap(List.class, returnType);
    if (opt.isPresent()) {
      Optionalish optional = opt.get();
      // optional match
      ParameterSpec param = basicInfo.constructorParam(optional.liftedType());
      return new AutoAttempt(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL);
    }
    if (listWrapped.isPresent()) {
      // repeatable match
      ParameterSpec param = basicInfo.constructorParam(returnType);
      return new AutoAttempt(listWrapped.get(), param, REPEATABLE);
    }
    // exact match (-> required)
    ParameterSpec param = basicInfo.constructorParam(returnType);
    return new AutoAttempt(tool().box(returnType), param, REQUIRED);
  }

  public Coercion findCoercion() {
    MapperAttempt attempt = getAttempt();
    return attempt.findCoercion(basicInfo)
        .orElseThrow(basicInfo::failure);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
