package net.jbock.coerce.mapperpresent;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.CanonicalOptional;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.ParameterType.OPTIONAL;
import static net.jbock.coerce.ParameterType.REPEATABLE;
import static net.jbock.coerce.ParameterType.REQUIRED;

public class CollectorAbsentMapperPresent {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  CollectorAbsentMapperPresent(BasicInfo basicInfo, TypeElement mapperClass) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  private List<Attempt> getAttempts() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<CanonicalOptional> canonicalOptional = CanonicalOptional.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    List<Attempt> attempts = new ArrayList<>();
    canonicalOptional.ifPresent(optional -> {
      // optional wrapped attempt
      attempts.add(new Attempt(optional.wrappedType(), optional.extractExpr(), optional.liftedType(), OPTIONAL, mapperClass, basicInfo));
      // optional lifted type attempt
      attempts.add(new Attempt(optional.liftedType(), optional.extractExpr(), optional.liftedType(), REQUIRED, mapperClass, basicInfo));
    });
    list.ifPresent(wrapped ->
        // repeatable attempt
        attempts.add(new Attempt(wrapped, p -> CodeBlock.of("$N", p), returnType, REPEATABLE, mapperClass, basicInfo)));
    // required attempt (exact match)
    attempts.add(new Attempt(tool().box(returnType), p -> CodeBlock.of("$N", p), returnType, REQUIRED, mapperClass, basicInfo));
    return attempts;
  }

  public Coercion findCoercion() {
    List<Attempt> attempts = getAttempts();
    Either<Coercion, String> either = null;
    for (Attempt attempt : attempts) {
      either = attempt.findCoercion();
      if (either instanceof Left) {
        return ((Left<Coercion, String>) either).value();
      }
    }
    if (either == null) { // impossible: there is always the "exact match" attempt
      throw new AssertionError();
    }
    String message = ((Right<Coercion, String>) either).value();
    throw basicInfo.asValidationException(message);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
