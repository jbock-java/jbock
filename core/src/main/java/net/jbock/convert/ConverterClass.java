package net.jbock.convert;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class ConverterClass {

  private final Optional<TypeElement> converter;

  public ConverterClass(Optional<TypeElement> converter) {
    this.converter = converter;
  }

  public Optional<TypeElement> converter() {
    return converter;
  }

  public boolean isPresent() {
    return converter.isPresent();
  }
}
