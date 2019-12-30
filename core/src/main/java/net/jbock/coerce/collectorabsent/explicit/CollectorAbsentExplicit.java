package net.jbock.coerce.collectorabsent.explicit;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collectorabsent.Optionalish;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.ParameterStyle.OPTIONAL;
import static net.jbock.coerce.ParameterStyle.REPEATABLE;
import static net.jbock.coerce.ParameterStyle.REQUIRED;

public class CollectorAbsentExplicit {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  public CollectorAbsentExplicit(BasicInfo basicInfo, TypeElement mapperClass) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  private List<ExplicitAttempt> getAttempts() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    List<ExplicitAttempt> attempts = new ArrayList<>();
    opt.ifPresent(optional -> {
      // optional wrapped attempt
      attempts.add(new ExplicitAttempt(optional.wrappedType(), optional.extractExpr(), optional.liftedType(), OPTIONAL, mapperClass, basicInfo));
      // optional lifted type attempt
      attempts.add(new ExplicitAttempt(optional.liftedType(), optional.extractExpr(), optional.liftedType(), REQUIRED, mapperClass, basicInfo));
    });
    list.ifPresent(wrapped ->
        // repeatable attempt
        attempts.add(new ExplicitAttempt(wrapped, p -> CodeBlock.of("$N", p), returnType, REPEATABLE, mapperClass, basicInfo)));
    // required attempt (exact match)
    attempts.add(new ExplicitAttempt(tool().box(returnType), p -> CodeBlock.of("$N", p), returnType, REQUIRED, mapperClass, basicInfo));
    return attempts;
  }

  public Coercion findCoercion() {
    List<ExplicitAttempt> attempts = getAttempts();
    Either<String, Coercion> either = null;
    for (ExplicitAttempt attempt : attempts) {
      either = attempt.findCoercion();
      if (either instanceof Right) {
        return ((Right<String, Coercion>) either).value();
      }
    }
    if (either == null) { // impossible: there is always the "exact match" attempt
      throw new AssertionError();
    }
    String message = ((Left<String, Coercion>) either).value();
    throw basicInfo.asValidationException(message);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
