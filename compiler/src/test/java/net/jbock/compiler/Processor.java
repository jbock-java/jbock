package net.jbock.compiler;

class Processor {

  static JbockProcessor testInstance() {
    String key = "jbock.test";
    boolean env = "true".equals(System.getenv(key));
    boolean prop = "true".equals(System.getProperty(key));
    boolean test = env || prop;
    return new JbockProcessor(test);
  }
}
