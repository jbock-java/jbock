package net.jbock.coerce.mappers;

import net.jbock.coerce.Coercion;
import net.jbock.coerce.TriggerKind;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

import static net.jbock.compiler.Constants.STRING;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final TypeMirror mapperType;

  private MapperCoercion(TypeMirror trigger, ParameterSpec mapperParam, TypeMirror mapperType) {
    super(trigger);
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(TriggerKind tk, ParameterSpec mapperParam, TypeMirror mapperType, FieldSpec field) {
    return new MapperCoercion(tk.trigger, mapperParam, mapperType).getCoercion(field, tk);
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

  public static CodeBlock mapperInit(TypeMirror trigger, ParameterSpec mapperParam, TypeMirror mapperType) {
    return mapperInit(TypeName.get(trigger), mapperParam, mapperType);
  }

  public static CodeBlock mapperInit(TypeName trigger, ParameterSpec mapperParam, TypeMirror mapperType) {
    return CodeBlock.builder()
        .add("$T $N = new $T().get()", ParameterizedTypeName.get(ClassName.get(Function.class), STRING, trigger), mapperParam,
            TypeTool.get().erasure(mapperType))
        .build();
  }
}
