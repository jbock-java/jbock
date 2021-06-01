package net.jbock.context;

public abstract class Cached<E> {

  private E instance;

  abstract E define();

  public final E get() {
    if (instance == null) {
      instance = define();
    }
    return instance;
  }
}
