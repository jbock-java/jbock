package net.jbock.coerce.collectors;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

/**
 * No collector class specified.
 * Collect into {@link java.util.List}.
 */
public class DefaultCollector extends AbstractCollector {

  public DefaultCollector(TypeMirror inputType) {
    super(inputType);
  }

  @Override
  public CodeBlock collectExpr() {
    return CodeBlock.of(".collect($T.toList())", Collectors.class);
  }
}
