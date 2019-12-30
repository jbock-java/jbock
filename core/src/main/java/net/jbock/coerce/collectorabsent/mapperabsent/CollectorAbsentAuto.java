package net.jbock.coerce.collectorabsent.mapperabsent;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collectorabsent.CanonicalOptional;
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

  private AutoAttempt getAttempt() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<CanonicalOptional> canonicalOptional = CanonicalOptional.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    if (canonicalOptional.isPresent()) {
      CanonicalOptional optional = canonicalOptional.get();
      // optional attempt
      return new AutoAttempt(optional.wrappedType(), optional.extractExpr(), optional.liftedType(), OPTIONAL, basicInfo);
    }
    if (list.isPresent()) {
      // repeatable attempt
      return new AutoAttempt(list.get(), p -> CodeBlock.of("$N", p), returnType, REPEATABLE, basicInfo);
    }
    // required attempt (exact match)
    return new AutoAttempt(returnType, p -> CodeBlock.of("$N", p), returnType, REQUIRED, basicInfo);
  }

  public Coercion findCoercion() {
    AutoAttempt attempt = getAttempt();
    return attempt.findCoercion()
        .orElseThrow(basicInfo::asValidationException);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
