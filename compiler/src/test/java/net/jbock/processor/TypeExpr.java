package net.jbock.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convenience for creating declared types that contain generics, like
 * {@code Set<String>}
 */
public class TypeExpr {

  private final String name;
  private final List<TypeExpr> args;

  private TypeExpr(
      String name,
      List<TypeExpr> args) {
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
      TypeExpr arg = args.get(i);
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
    sb.append(args.stream().map(TypeExpr::toString).collect(Collectors.joining(", ")));
    sb.append('>');
    return sb.toString();
  }

  public static class Builder {
    private final Elements elements;
    private final Types types;

    private Builder(Elements elements, Types types) {
      this.elements = elements;
      this.types = types;
    }

    public DeclaredType parse(String expr) {
      return doParse(expr).toType(elements, types);
    }

    private TypeExpr doParse(String expr) {
      StringBuilder nameBuilder = new StringBuilder();
      StringBuilder currentArg = new StringBuilder();
      List<TypeExpr> args = new ArrayList<>();
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
            args.add(doParse(currentArg.toString()));
            currentArg.delete(0, currentArg.length());
          }
          if (depth <= -1) {
            throw new IllegalArgumentException("unmatched closing bracket");
          }
        } else if (depth >= 2) {
          currentArg.append(c);
        } else if (depth == 1) {
          if (c == ',') {
            args.add(doParse(currentArg.toString()));
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
      return new TypeExpr(nameBuilder.toString(), args);
    }
  }

  public static Builder prepare(Elements elements, Types types) {
    return new Builder(elements, types);
  }
}
