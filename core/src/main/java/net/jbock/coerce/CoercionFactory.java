package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Objects;
import java.util.Optional;

abstract class CoercionFactory {

  final TypeName trigger;

  CoercionFactory(Class<?> trigger) {
    this(TypeName.get(trigger));
  }

  CoercionFactory(TypeName trigger) {
    this.trigger = trigger;
  }

  /**
   * Maps from String to trigger type
   */
  abstract CodeBlock map();

  /**
   * Type that triggers this coercion (could be wrapped in Optional or List)
   */
  final TypeName trigger() {
    return trigger;
  }

  // toString stuff
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($T.toString($L))", Objects.class, param).build();
  }

  // toString stuff
  CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($T::toString).map(quote)", Objects.class).build();
  }

  /**
   * Specials can't be in Optional or List
   */
  boolean special() {
    return false;
  }

  Optional<CodeBlock> initMapper() {
    return Optional.empty();
  }

  TypeName paramType() {
    return trigger;
  }

  final Coercion getCoercion(
      FieldSpec field,
      CoercionKind kind) {
    return new Coercion(
        trigger,
        map(),
        special(),
        initMapper(),
        jsonExpr(field.name),
        mapJsonExpr(field),
        paramType(),
        field,
        kind);
  }
}
