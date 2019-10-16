package net.jbock.coerce.collector;

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
  public CodeBlock createCollector() {
    return CodeBlock.of("$T.toList()", Collectors.class);
  }
}
