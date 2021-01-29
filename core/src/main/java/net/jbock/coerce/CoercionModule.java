package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import dagger.Module;
import dagger.Provides;
import net.jbock.compiler.ConstructorParam;
import net.jbock.compiler.ExtractExpr;
import net.jbock.compiler.MapExpr;
import net.jbock.compiler.TailExpr;

@Module
class CoercionModule {

  private final CodeBlock tailExpr;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final ParameterSpec constructorParam;

  CoercionModule(CodeBlock tailExpr, CodeBlock mapExpr, CodeBlock extractExpr, ParameterSpec constructorParam) {
    this.tailExpr = tailExpr;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.constructorParam = constructorParam;
  }

  @TailExpr
  @Provides
  CodeBlock tailExpr() {
    return tailExpr;
  }

  @MapExpr
  @Provides
  CodeBlock mapExpr() {
    return mapExpr;
  }

  @ExtractExpr
  @Provides
  CodeBlock extractExpr() {
    return extractExpr;
  }

  @ConstructorParam
  @Provides
  public ParameterSpec constructorParam() {
    return constructorParam;
  }
}
