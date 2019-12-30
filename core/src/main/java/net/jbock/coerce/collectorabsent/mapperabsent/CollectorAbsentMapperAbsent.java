package net.jbock.coerce.collectorabsent.mapperabsent;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collectorabsent.CanonicalOptional;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.ParameterType.OPTIONAL;
import static net.jbock.coerce.ParameterType.REPEATABLE;
import static net.jbock.coerce.ParameterType.REQUIRED;

public class CollectorAbsentMapperAbsent {

  private final BasicInfo basicInfo;

  public CollectorAbsentMapperAbsent(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  private Attempt getAttempt() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<CanonicalOptional> canonicalOptional = CanonicalOptional.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    if (canonicalOptional.isPresent()) {
      CanonicalOptional optional = canonicalOptional.get();
      // optional attempt
      return new Attempt(optional.wrappedType(), optional.extractExpr(), optional.liftedType(), OPTIONAL, basicInfo);
    }
    if (list.isPresent()) {
      // repeatable attempt
      return new Attempt(list.get(), p -> CodeBlock.of("$N", p), returnType, REPEATABLE, basicInfo);
    }
    // required attempt (exact match)
    return new Attempt(returnType, p -> CodeBlock.of("$N", p), returnType, REQUIRED, basicInfo);
  }

  public Coercion findCoercion() {
    Attempt attempt = getAttempt();
    return attempt.findCoercion()
        .orElseThrow(basicInfo::asValidationException);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
