package net.jbock.coerce.mappers;

import net.jbock.coerce.Coercion;
import net.jbock.coerce.TriggerKind;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;
  private final TypeName mapperType;

  private MapperCoercion(TypeMirror trigger, ParameterSpec mapperParam, TypeName mapperType) {
    super(TypeName.get(trigger));
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(TriggerKind tk, ParameterSpec mapperParam, TypeName mapperType, FieldSpec field) {
    return new MapperCoercion(tk.trigger, mapperParam, mapperType).getCoercion(field, tk.kind);
  }

  @Override
  public CodeBlock map() {
    return mapperMap(mapperParam);
  }

  @Override
  public CodeBlock initMapper() {
    return mapperInit(trigger, mapperParam, mapperType);
  }

  public static CodeBlock mapperMap(ParameterSpec mapperParam) {
    return CodeBlock.builder().add(".map($N)", mapperParam).build();
  }

  public static CodeBlock mapperInit(TypeMirror trigger, ParameterSpec mapperParam, TypeName mapperType) {
    return mapperInit(TypeName.get(trigger), mapperParam, mapperType);
  }

  public static CodeBlock mapperInit(TypeName trigger, ParameterSpec mapperParam, TypeName mapperType) {
    return CodeBlock.builder()
        .add("$T $N = new $T().get()", ParameterizedTypeName.get(ClassName.get(Function.class), STRING, trigger), mapperParam, mapperType)
        .build();
  }
}
