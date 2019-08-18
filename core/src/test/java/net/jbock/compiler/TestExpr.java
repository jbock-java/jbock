package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// test util
public class TestExpr {

  private final String name;
  private final List<TestExpr> args;

  private TestExpr(String name, List<TestExpr> args) {
    this.name = name;
    this.args = args;
  }

  private DeclaredType toType(Elements elements, Types types) {
    TypeElement typeElement = elements.getTypeElement(name);
    if (typeElement == null) {
      throw new IllegalArgumentException("unknown type: " + name);
    }
    if (args.isEmpty()) {
      return types.getDeclaredType(typeElement);
    }
    TypeMirror[] typeArgs = new TypeMirror[args.size()];
    for (int i = 0; i < args.size(); i++) {
      TestExpr arg = args.get(i);
      typeArgs[i] = arg.toType(elements, types);
    }
    return types.getDeclaredType(typeElement, typeArgs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(name);
    if (args.isEmpty()) {
      return sb.toString();
    }
    sb.append('<');
    sb.append(args.stream().map(TestExpr::toString).collect(Collectors.joining(", ")));
    sb.append('>');
    return sb.toString();
  }

  public static DeclaredType parse(String expr, Elements elements, Types types) {
    return _parse(expr).toType(elements, types);
  }

  private static TestExpr _parse(String expr) {
    StringBuilder nameBuilder = new StringBuilder();
    StringBuilder currentArg = new StringBuilder();
    List<TestExpr> args = new ArrayList<>();
    int depth = 0;
    for (char c : expr.toCharArray()) {
      if (c == '<') {
        if (depth == 0 && !args.isEmpty()) {
          throw new IllegalArgumentException("illegal expression");
        }
        depth++;
        if (depth >= 2) {
          currentArg.append('<');
        }
      } else if (c == '>') {
        if (depth >= 2) {
          currentArg.append('>');
        }
        depth--;
        if (depth == 0) {
          args.add(_parse(currentArg.toString()));
          currentArg.delete(0, currentArg.length());
        }
        if (depth <= -1) {
          throw new IllegalArgumentException("unmatched closing bracket");
        }
      } else if (depth >= 2) {
        currentArg.append(c);
      } else if (depth == 1) {
        if (c == ',') {
          args.add(_parse(currentArg.toString()));
          currentArg.delete(0, currentArg.length());
        } else if (!Character.isWhitespace(c)) {
          currentArg.append(c);
        }
      } else if (depth == 0) {
        nameBuilder.append(c);
      }
    }
    if (depth > 0) {
      throw new IllegalArgumentException("unmatched opening bracket");
    }
    return new TestExpr(nameBuilder.toString(), args);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TestExpr expr = (TestExpr) o;
    return Objects.equals(name, expr.name) &&
        Objects.equals(args, expr.args);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, args);
  }
}
