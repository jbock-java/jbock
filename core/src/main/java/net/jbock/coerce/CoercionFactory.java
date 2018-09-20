package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
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

  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$T.requireNonNull($N)", Objects.class, param).build();
  }

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
    // primitive optionals get special treatment here
    ParameterSpec param = ParameterSpec.builder(paramType(), field.name).build();
    Coercion coercion = new Coercion(
        trigger,
        map(),
        special(),
        initMapper(),
        jsonExpr(field.name),
        mapJsonExpr(field),
        extract(param),
        paramType(),
        field,
        kind);
    switch (kind) {
      case LIST_COMBINATION:
        return coercion.asList();
      case OPTIONAL_COMBINATION:
        return coercion.asOptional();
      default:
        return coercion;
    }
  }
}
