package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

@Command
abstract class MvArguments implements MvArguments_Parent, MvArguments_ParentParent {

  @Parameter(index = 1)
  abstract String dest();

  @Override
  public boolean isSafe() {
    return true;
  }
}
