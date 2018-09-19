package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Objects;
import java.util.Optional;

abstract class CoercionFactory {

  private final TypeName trigger;

  CoercionFactory(Class<?> trigger) {
    this(TypeName.get(trigger));
  }

  CoercionFactory(TypeName trigger) {
    this.trigger = trigger;
  }

  /**
   * Maps from String to trigger type
   */
  public abstract CodeBlock map();

  /**
   * Type that triggers this coercion (could be wrapped in Optional or List)
   */
  public final TypeName trigger() {
    return trigger;
  }

  // toString stuff (the gen class overrides toString if possible)
  public final CodeBlock jsonExpr(FieldSpec field) {
    return jsonExpr(field.name);
  }

  // toString stuff
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($T.toString($L))", Objects.class, param).build();
  }

  // toString stuff
  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($T::toString).map(quote)", Objects.class).build();
  }

  /**
   * Specials can't be in Optional or List
   */
  public boolean special() {
    return false;
  }

  public Optional<CodeBlock> initMapper() {
    return Optional.empty();
  }

  public TypeName paramType() {
    return trigger;
  }

  final Coercion getCoercion(FieldSpec field) {
    return new Coercion(trigger, map(), special(), initMapper(), paramType(), field) {
      @Override
      CodeBlock jsonExpr(String param) {
        return CoercionFactory.this.jsonExpr(param);
      }

      @Override
      public CodeBlock mapJsonExpr() {
        return CoercionFactory.this.mapJsonExpr(field);
      }
    };
  }
}
