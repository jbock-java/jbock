package net.jbock.coerce.collector;

import javax.lang.model.type.TypeMirror;

/**
 * No collector class specified. Collect to {@link java.util.List List}.
 */
public class DefaultCollector extends AbstractCollector {
  public DefaultCollector(TypeMirror inputType) {
    super(inputType);
  }
}
