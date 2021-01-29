package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.mapper.MapperSuccess;
import net.jbock.compiler.EnumName;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import java.util.function.Function;

public class CoercionFactory {

  private final EnumName enumName;

  @Inject
  CoercionFactory(EnumName enumName) {
    this.enumName = enumName;
  }

  @Component(modules = CoercionModule.class)
  interface CoercionComponent {

    Coercion coercion();

    @Component.Builder
    interface Builder {

      @BindsInstance
      Builder enumName(EnumName enumName);

      @BindsInstance
      Builder skew(Skew skew);

      Builder coercionModule(CoercionModule module);

      CoercionComponent build();
    }
  }

  private Coercion createCoercion(
      CodeBlock mapExpr,
      CodeBlock tailExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec constructorParam) {
    return DaggerCoercionFactory_CoercionComponent.builder()
        .enumName(enumName)
        .skew(skew)
        .coercionModule(new CoercionModule(tailExpr, mapExpr, extractExpr, constructorParam))
        .build()
        .coercion();
  }

  public Coercion create(Match match, CodeBlock mapExpr) {
    CodeBlock tailExpr = match.tailExpr();
    CodeBlock extractExpr = match.extractExpr();
    Skew skew = match.skew();
    ParameterSpec constructorParam = match.constructorParam();
    return createCoercion(mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }

  public Coercion create(MapperSuccess success) {
    CodeBlock mapExpr = success.mapExpr();
    CodeBlock tailExpr = success.tailExpr();
    CodeBlock extractExpr = success.extractExpr();
    Skew skew = success.skew();
    ParameterSpec constructorParam = success.constructorParam();
    return createCoercion(mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }

  public Coercion createFlag(ExecutableElement sourceMethod) {
    ParameterSpec constructorParam = ParameterSpec.builder(TypeName.get(sourceMethod.getReturnType()), enumName.snake()).build();
    CodeBlock mapExpr = CodeBlock.of("$T.identity()", Function.class);
    CodeBlock tailExpr = CodeBlock.of(".findAny().isPresent()");
    CodeBlock extractExpr = CodeBlock.of("$N", constructorParam);
    return createCoercion(mapExpr, tailExpr, extractExpr, Skew.FLAG, constructorParam);
  }
}
