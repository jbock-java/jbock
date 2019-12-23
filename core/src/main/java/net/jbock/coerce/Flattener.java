package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

public class Flattener {

  private final BasicInfo basicInfo;
  private final TypeElement targetElement;

  public Flattener(BasicInfo basicInfo, TypeElement targetElement) {
    this.targetElement = targetElement;
    this.basicInfo = basicInfo;
  }

  /**
   * @param leftSolution a mapping
   * @param rightSolution a mapping
   * @return type parameters in the correct order for {@code targetElement}
   */
  Either<String, FlattenerResult> getTypeParameters(TypevarMapping leftSolution, TypevarMapping rightSolution) {
    return leftSolution.merge(rightSolution).flatMap(Function.identity(),
        this::getTypeParameters);
  }

  public Either<String, FlattenerResult> getTypeParameters(TypevarMapping solution) {
    List<TypeMirror> result = new ArrayList<>();
    Map<String, TypeMirror> mapping = new LinkedHashMap<>();
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (TypeParameterElement p : parameters) {
      List<? extends TypeMirror> bounds = p.getBounds();
      TypeMirror m = solution.get(p.toString());
      if (m == null) {
        Either<String, TypeMirror> inferred = inferFromBounds(p, bounds);
        if (inferred instanceof Left) {
          return left(((Left<String, TypeMirror>) inferred).value());
        }
        m = ((Right<String, TypeMirror>) inferred).value();
      }
      if (tool().isOutOfBounds(m, bounds)) {
        return left("Invalid bounds: Can't resolve " + p.toString() + " to " + m);
      }
      mapping.put(p.toString(), m);
      result.add(m);
    }
    return right(new FlattenerResult(result, new TypevarMapping(mapping, tool())));
  }

  private Either<String, TypeMirror> inferFromBounds(TypeParameterElement p, List<? extends TypeMirror> bounds) {
    if (bounds.isEmpty()) {
      return right(tool().getDeclaredType(Object.class, Collections.emptyList()));
    }
    if (bounds.size() >= 2) {
      return left("Intersection type is not supported for typevar " + p.toString());
    }
    return right(bounds.get(0));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
