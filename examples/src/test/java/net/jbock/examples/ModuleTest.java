package net.jbock.examples;

import static org.junit.Assert.assertEquals;

import net.jbock.CommandLineArguments;
import org.junit.Test;

public class ModuleTest {

  @Test
  public void testModuleName() {
    assertEquals("net.jbock", CommandLineArguments.class.getModule().getName());
  }
}
