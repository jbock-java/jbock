package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Flattener {

  private final BasicInfo basicInfo;
  private final TypeElement targetElement;

  public Flattener(BasicInfo basicInfo, TypeElement targetElement) {
    this.targetElement = targetElement;
    this.basicInfo = basicInfo;
  }

  /**
   * @param partialSolutions named type parameters
   * @return type parameters in the correct order for {@code targetElement}
   */
  public Either<List<TypeMirror>, String> getTypeParameters(List<Map<String, TypeMirror>> partialSolutions) {
    Either<Map<String, TypeMirror>, String> result = mergeResult(partialSolutions);
    return result.flatLeftMap(this::getTypeParameters, Function.identity());
  }

  Either<List<TypeMirror>, String> getTypeParameters(Map<String, TypeMirror> solution) {
    List<? extends TypeParameterElement> typeParameters = targetElement.getTypeParameters();
    List<TypeMirror> outcome = new ArrayList<>();
    for (TypeParameterElement p : typeParameters) {
      Either<TypeMirror, String> resolved = resolve(solution, p);
      if (resolved instanceof Right) {
        return Either.right(((Right<TypeMirror, String>) resolved).value());
      }
      outcome.add(((Left<TypeMirror, String>) resolved).value());
    }
    return Either.left(outcome);
  }

  private Either<TypeMirror, String> resolve(Map<String, TypeMirror> result, TypeParameterElement typeParameter) {
    TypeMirror m = result.get(typeParameter.toString());
    List<? extends TypeMirror> bounds = typeParameter.getBounds();
    if (m != null) {
      if (tool().isOutOfBounds(m, bounds)) {
        return Either.right("invalid bounds");
      }
    }
    return Either.left(m);
  }

  private Either<Map<String, TypeMirror>, String> mergeResult(List<Map<String, TypeMirror>> results) {
    Map<String, TypeMirror> out = new LinkedHashMap<>();
    for (Map<String, TypeMirror> result : results) {
      for (Map.Entry<String, TypeMirror> entry : result.entrySet()) {
        TypeMirror current = out.get(entry.getKey());
        if (current != null) {
          if (!tool().isSameType(current, entry.getValue())) {
            return Either.right("invalid bounds");
          }
        }
        out.put(entry.getKey(), entry.getValue());
      }
    }
    return Either.left(out);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
