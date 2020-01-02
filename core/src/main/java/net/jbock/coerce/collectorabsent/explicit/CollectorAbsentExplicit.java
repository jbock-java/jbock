package net.jbock.coerce.collectorabsent.explicit;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collectorabsent.MapperAttempt;
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

  private List<MapperAttempt> getAttempts() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    List<MapperAttempt> attempts = new ArrayList<>();
    opt.ifPresent(optional -> {
      ParameterSpec param = basicInfo.param(optional.liftedType());
      // optional match
      attempts.add(new ExplicitAttempt(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL, mapperClass));
      // exact match (-> required)
      attempts.add(new ExplicitAttempt(optional.liftedType(), optional.extractExpr(param), param, REQUIRED, mapperClass));
    });
    list.ifPresent(wrapped -> {
      ParameterSpec param = basicInfo.param(returnType);
      // list match
      attempts.add(new ExplicitAttempt(wrapped, CodeBlock.of("$N", param), param, REPEATABLE, mapperClass));
    });
    ParameterSpec param = basicInfo.param(returnType);
    // exact match (-> required)
    attempts.add(new ExplicitAttempt(tool().box(returnType), CodeBlock.of("$N", param), param, REQUIRED, mapperClass));
    return attempts;
  }

  public Coercion findCoercion() {
    List<MapperAttempt> attempts = getAttempts();
    Either<String, Coercion> either = null;
    for (MapperAttempt attempt : attempts) {
      either = attempt.findCoercion(basicInfo);
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
