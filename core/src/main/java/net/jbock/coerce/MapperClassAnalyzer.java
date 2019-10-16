package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.AbstractReferencedType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ReferenceTool.Expectation.MAPPER;

// for when there's no collector
final class MapperClassAnalyzer {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  MapperClassAnalyzer(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  static class Failure {
    private final String message;

    Failure(String message) {
      this.message = message;
    }

    String getMessage() {
      return String.format("There is a problem with the mapper class: %s.", message);
    }
  }

  private static Failure failure(String message) {
    return new Failure(message);
  }

  Either<ReferenceMapperType, Failure> checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    AbstractReferencedType functionType = new ReferenceTool(MAPPER, basicInfo, mapperClass)
        .getReferencedType();
    TypeMirror t = functionType.expectedType.getTypeArguments().get(0);
    TypeMirror r = functionType.expectedType.getTypeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(tool().asType(String.class), t)
        .map(functionType::mapTypevars);
    if (!t_result.isPresent()) {
      return Either.right(failure(String.format("The supplied function must take a String argument, but takes %s", t)));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (r_result.isPresent()) {
      if (!checkCompat(t_result.get(), r_result.get())) {
        return Either.right(failure("could not infer type parameters"));
      }
    }
    if (!r_result.isPresent()) {
      return Either.right(failure(String.format("The mapper should return %s but returns %s", expectedReturnType, r)));
    }
    Either<ReferenceMapperType, String> solve = new Solver(functionType, t_result.get(), r_result.get(), functionType.mapTypevars(r_result.get())).solve();
    if (solve instanceof Right) {
      return Either.right(failure(((Right<ReferenceMapperType, String>) solve).value()));
    }
    return Either.left(((Left<ReferenceMapperType, String>) solve).value());
  }

  private boolean checkCompat(Map<String, TypeMirror> t_result, Map<String, TypeMirror> r_result) {
    if (t_result.isEmpty()) {
      return true;
    }
    Map.Entry<String, TypeMirror> tEntry = t_result.entrySet().iterator().next();
    String key = tEntry.getKey();
    TypeMirror tSolution = tEntry.getValue();
    TypeMirror rSolution = r_result.get(key);
    if (rSolution == null) {
      return true;
    }
    return tool().isSameType(tSolution, rSolution);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private class Solver {

    final AbstractReferencedType functionType;
    final Map<String, TypeMirror> t_result;
    final Map<String, TypeMirror> unmapped_r_result;
    final Map<String, TypeMirror> r_result;

    Solver(
        AbstractReferencedType functionType,
        Map<String, TypeMirror> t_result,
        Map<String, TypeMirror> unmapped_r_result,
        Map<String, TypeMirror> r_result) {
      this.functionType = functionType;
      this.t_result = t_result;
      this.unmapped_r_result = unmapped_r_result;
      this.r_result = r_result;
    }

    Either<ReferenceMapperType, String> solve() {
      List<TypeMirror> solution = new ArrayList<>();
      for (TypeParameterElement typeMirror : mapperClass.getTypeParameters()) {
        Either<TypeMirror, String> either = getSolution(typeMirror);
        if (either instanceof Right) {
          return Either.right(((Right<TypeMirror, String>) either).value());
        }
        solution.add(((Left<TypeMirror, String>) either).value());
      }
      return Either.left(MapperType.create(tool(), functionType.isSupplier(), mapperClass, solution));
    }

    Either<TypeMirror, String> getSolution(TypeParameterElement typeParameter) {
      TypeMirror t = t_result.get(typeParameter.toString());
      TypeMirror r = r_result.get(typeParameter.toString());
      List<? extends TypeMirror> bounds = typeParameter.getBounds();
      if (t != null) {
        if (tool().isOutOfBounds(tool().asType(String.class), bounds)) {
          return Either.right("invalid bounds");
        }
      }
      if (r != null) {
        if (tool().isOutOfBounds(r, bounds)) {
          return Either.right("invalid bounds");
        }
      }
      return Stream.of(t, r)
          .filter(Objects::nonNull)
          .map(Either::<TypeMirror, String>left)
          .findFirst()
          .orElseGet(() -> Either.right("could not infer all type parameters"));
    }
  }
}
