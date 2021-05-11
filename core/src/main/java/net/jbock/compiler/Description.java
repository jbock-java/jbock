package net.jbock.compiler;

import java.util.Arrays;
import java.util.List;

public class Description {

  private final List<String> lines;

  public Description(String[] value) {
    this.lines = Arrays.asList(value);
  }

  public List<String> lines() {
    return lines;
  }
}
