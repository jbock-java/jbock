package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static net.jbock.coerce.Util.getTypeParameterList;

class CollectorInfo {

  // For custom collector this is the T in Collector<T, A, R>.
  // For default collector it is the E in List<E>.
  private final TypeMirror inputType;
  private final CodeBlock collectExpr;

  private CollectorInfo(TypeMirror inputType, CodeBlock collectExpr) {
    this.inputType = inputType;
    this.collectExpr = collectExpr;
  }

  static CollectorInfo create(TypeTool tool, TypeMirror inputType, TypeElement collectorClass, boolean supplier, List<TypeMirror> solution) {
    return new CollectorInfo(inputType, CodeBlock.of(".collect(new $T$L()$L)",
        tool.erasure(collectorClass.asType()),
        getTypeParameterList(solution),
        supplier ? ".get()" : ""));
  }

  TypeMirror inputType() {
    return inputType;
  }

  CodeBlock collectExpr() {
    return collectExpr;
  }
}
