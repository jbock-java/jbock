package net.jbock.coerce.mappers;

import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionKind;
import net.jbock.coerce.CollectorInfo;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.WildcardTypeName;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

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

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  TypeName paramType() {
    return trigger;
  }

  public final Coercion getCoercion(
      FieldSpec field,
      CoercionKind kind,
      CollectorInfo collectorInfo) {
    // primitive optionals get special treatment here
    ParameterSpec param = ParameterSpec.builder(paramType(), field.name).build();
    Coercion coercion = new Coercion(
        trigger,
        Optional.ofNullable(collectorParam(field, collectorInfo)),
        map(),
        initMapper(),
        initCollector(field, collectorInfo),
        jsonExpr(field.name),
        mapJsonExpr(field),
        extract(param),
        paramType(),
        field,
        kind);
    if (!collectorInfo.isEmpty()) {
      return coercion.asList(collectorInfo);
    }
    switch (kind) {
      case OPTIONAL_COMBINATION:
        return coercion.asOptional();
      default:
        return coercion;
    }
  }

  private CodeBlock initCollector(
      FieldSpec field,
      CollectorInfo collectorInfo) {
    ParameterSpec collectorParam = collectorParam(field, collectorInfo);
    if (collectorParam == null) {
      return CodeBlock.builder().build();
    }
    return collectorInfo.collectorInit;

  }

  private ParameterSpec collectorParam(
      FieldSpec field,
      CollectorInfo collectorInfo) {
    if (collectorInfo.isEmpty()) {
      return null;
    }
    return ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class),
        TypeName.get(collectorInfo.collectorInput),
        WildcardTypeName.subtypeOf(Object.class),
        field.type), field.name + "Collector")
        .build();
  }
}
