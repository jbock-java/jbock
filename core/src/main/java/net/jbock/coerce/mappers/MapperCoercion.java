package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CollectorType;
import net.jbock.coerce.MapperType;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public final class MapperCoercion extends CoercionFactory {

  private final ParameterSpec mapperParam;

  private final MapperType mapperType;

  private MapperCoercion(
      TypeMirror mapperReturnType,
      ParameterSpec mapperParam,
      MapperType mapperType) {
    super(mapperReturnType);
    this.mapperParam = mapperParam;
    this.mapperType = mapperType;
  }

  public static Coercion create(
      TypeMirror mapperReturnType,
      Optional<CollectorType> collectorType,
      ParameterSpec mapperParam,
      MapperType mapperType,
      BasicInfo basicInfo) {
    return new MapperCoercion(mapperReturnType, mapperParam, mapperType)
        .getCoercion(basicInfo, collectorType);
  }

  @Override
  public Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$N", mapperParam));
  }

  @Override
  public CodeBlock initMapper() {
    CodeBlock typeParameters = getTypeParameters();
    return CodeBlock.of("$T $N = new $T$L",
        mapperParam.type,
        mapperParam,
        mapperType.mapperType(),
        typeParameters);
  }

  private CodeBlock getTypeParameters() {
    if (!mapperType.hasTypeParams()) {
      if (!mapperType.supplier()) {
        // new Mapper();
        return CodeBlock.of("()");
      } else {
        // new Mapper().get();
        return CodeBlock.of("().get()");
      }
    }
    if (!mapperType.supplier()) {
      // new Mapper<>();
      return CodeBlock.of("<>()");
    }
    CodeBlock.Builder typeParameterList = CodeBlock.builder();
    // compiler can't handle new Mapper<>().get();
    // needs explicit type params
    typeParameterList.add("<");
    List<TypeMirror> solution = mapperType.solution();
    for (int i = 0; i < solution.size(); i++) {
      TypeMirror typeMirror = solution.get(i);
      typeParameterList.add("$T", typeMirror);
      if (i < solution.size() - 1) {
        typeParameterList.add(", ");
      }
    }
    typeParameterList.add(">().get()");
    return typeParameterList.build();
  }
}
