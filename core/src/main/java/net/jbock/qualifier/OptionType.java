package net.jbock.qualifier;

import com.squareup.javapoet.ClassName;

public class OptionType {

  private final ClassName optionType;

  public OptionType(ClassName optionType) {
    this.optionType = optionType;
  }

  public ClassName type() {
    return optionType;
  }
}
