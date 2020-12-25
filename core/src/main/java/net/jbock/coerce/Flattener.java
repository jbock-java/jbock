package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

public class Flattener {

  private final TypeTool tool;
  private final TypeElement targetElement;
  private final Function<String, ValidationException> errorHandler;

  public Flattener(TypeTool tool, TypeElement targetElement, Function<String, ValidationException> errorHandler) {
    this.tool = tool;
    this.targetElement = targetElement;
    this.errorHandler = errorHandler;
  }

  /**
   * @param leftSolution a mapping
   * @param rightSolution a mapping
   * @return type parameters in the correct order for {@code targetElement}
   */
  public Either<String, FlattenerResult> mergeSolutions(TypevarMapping leftSolution, TypevarMapping rightSolution) {
    return leftSolution.merge(rightSolution).flatMap(Function.identity(),
        this::getTypeParameters);
  }

  private Either<String, FlattenerResult> getTypeParameters(TypevarMapping solution) {
    List<TypeMirror> result = new ArrayList<>();
    Map<String, TypeMirror> mapping = new LinkedHashMap<>(solution.getMapping());
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (TypeParameterElement p : parameters) {
      TypeMirror m = solution.get(p.toString());
      if (m == null || m.getKind() == TypeKind.TYPEVAR) {
        Either<String, TypeMirror> inferred = tool.getBound(p);
        if (inferred instanceof Left) {
          return left(((Left<String, TypeMirror>) inferred).value());
        }
        m = ((Right<String, TypeMirror>) inferred).value();
      }
      if (tool.isOutOfBounds(m, p.getBounds())) {
        return left("Invalid bounds: Can't resolve " + p.toString() + " to " + m);
      }
      mapping.put(p.toString(), m);
      result.add(m);
    }
    return right(new FlattenerResult(result, new TypevarMapping(mapping, tool, errorHandler)));
  }
}
