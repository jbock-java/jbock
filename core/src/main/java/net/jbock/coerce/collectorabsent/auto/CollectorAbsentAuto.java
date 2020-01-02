package net.jbock.coerce.collectorabsent.auto;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collectorabsent.MapperAttempt;
import net.jbock.coerce.collectorabsent.Optionalish;
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
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    if (opt.isPresent()) {
      Optionalish optional = opt.get();
      // optional attempt
      return new AutoAttempt(optional.wrappedType(), optional.extractExpr(), optional.liftedType(), OPTIONAL);
    }
    if (list.isPresent()) {
      // repeatable attempt
      return new AutoAttempt(list.get(), p -> CodeBlock.of("$N", p), returnType, REPEATABLE);
    }
    // required attempt (exact match)
    return new AutoAttempt(tool().box(returnType), p -> CodeBlock.of("$N", p), returnType, REQUIRED);
  }

  public Coercion findCoercion() {
    MapperAttempt attempt = getAttempt();
    return attempt.findCoercion(basicInfo)
        .orElseThrow(basicInfo::asValidationException);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
