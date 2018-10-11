package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionKind;
import net.jbock.coerce.CollectorInfo;
import net.jbock.coerce.TriggerKind;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

  public boolean handlesOptionalPrimitive() {
    return false;
  }

  final TypeMirror trigger;

  CoercionFactory(Class<?> trigger) {
    this(TypeTool.get().declared(trigger.getCanonicalName()));
  }

  CoercionFactory(TypeMirror trigger) {
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
  final TypeMirror trigger() {
    return trigger;
  }

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  TypeName paramType() {
    return TypeName.get(trigger);
  }

  public final Coercion getCoercion(
      FieldSpec field,
      TriggerKind tk) {
    return getCoercion(field, tk, map(), initMapper());
  }

  public final Coercion getCoercion(
      FieldSpec field,
      TriggerKind tk,
      CodeBlock map,
      CodeBlock initMapper) {
    CodeBlock extract;
    TypeName paramType;
    if (tk.kind == CoercionKind.OPTIONAL_COMBINATION || !tk.collectorInfo.isEmpty()) {
      ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
      paramType = field.type;
      extract = CodeBlock.builder().add("$T.requireNonNull($N)", Objects.class, param).build();
    } else {
      ParameterSpec param = ParameterSpec.builder(paramType(), field.name).build();
      paramType = paramType();
      extract = extract(param);
    }
    return new Coercion(
        trigger,
        Optional.ofNullable(collectorParam(field, tk.collectorInfo)),
        map,
        initMapper,
        initCollector(field, tk.collectorInfo),
        extract,
        paramType,
        field,
        tk.kind);
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
