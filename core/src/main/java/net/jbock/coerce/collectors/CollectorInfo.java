package net.jbock.coerce.collectors;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.stream.Collectors;

import static net.jbock.coerce.Util.getTypeParameterList;

public class CollectorInfo {

  // For custom collector this is the T in Collector<T, A, R>.
  // For default collector it is the E in List<E>.
  private final TypeMirror inputType;
  private final CodeBlock collectExpr;

  CollectorInfo(TypeMirror inputType, CodeBlock collectExpr) {
    this.inputType = inputType;
    this.collectExpr = collectExpr;
  }

  public static CollectorInfo createCustom(TypeTool tool, TypeMirror inputType, TypeElement collectorClass, boolean supplier, List<TypeMirror> solution) {
    return new CollectorInfo(inputType, CodeBlock.of(".collect(new $T$L()$L)",
        tool.erasure(collectorClass.asType()),
        getTypeParameterList(solution),
        supplier ? ".get()" : ""));
  }

  public static CollectorInfo createDefault(TypeMirror inputType) {
    return new CollectorInfo(inputType, CodeBlock.of(".collect($T.toList())", Collectors.class));
  }

  public TypeMirror inputType() {
    return inputType;
  }

  public CodeBlock collectExpr() {
    return collectExpr;
  }
}
