package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.Skew.OPTIONAL;
import static net.jbock.coerce.Skew.REPEATABLE;
import static net.jbock.coerce.Skew.REQUIRED;
import static net.jbock.coerce.either.Either.left;

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
    Optional<TypeMirror> listWrapped = tool().unwrap(List.class, returnType);
    List<MapperAttempt> attempts = new ArrayList<>();
    opt.ifPresent(optional -> {
      ParameterSpec param = basicInfo.constructorParam(optional.liftedType());
      // optional match
      attempts.add(attempt(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL));
      // exact match (-> required)
      attempts.add(attempt(optional.liftedType(), optional.extractExpr(param), param, REQUIRED));
    });
    listWrapped.ifPresent(wrapped -> {
      ParameterSpec param = basicInfo.constructorParam(returnType);
      // list match
      attempts.add(attempt(wrapped, param, REPEATABLE));
    });
    ParameterSpec param = basicInfo.constructorParam(returnType);
    // exact match (-> required)
    attempts.add(attempt(tool().box(returnType), param, REQUIRED));
    return attempts;
  }

  public Coercion findCoercion() {
    List<MapperAttempt> attempts = getAttempts();
    Either<String, Coercion> either = left("");
    for (MapperAttempt attempt : attempts) {
      either = attempt.findCoercion(basicInfo);
      if (either instanceof Right) {
        return ((Right<String, Coercion>) either).value();
      }
    }
    throw basicInfo.failure(((Left<String, Coercion>) either).value());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private MapperAttempt attempt(TypeMirror expectedReturnType, CodeBlock extractExpr, ParameterSpec constructorParam, Skew skew) {
    return new MapperAttempt(expectedReturnType, extractExpr, constructorParam, skew, mapperClass);
  }

  private MapperAttempt attempt(TypeMirror expectedReturnType, ParameterSpec constructorParam, Skew skew) {
    return new MapperAttempt(expectedReturnType, CodeBlock.of("$N", constructorParam), constructorParam, skew, mapperClass);
  }
}
