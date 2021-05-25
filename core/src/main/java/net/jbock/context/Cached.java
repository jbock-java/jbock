package net.jbock.context;

abstract class Cached<E> {

  private E instance;

  abstract E define();

  final E get() {
    if (instance == null) {
      instance = define();
    }
    return instance;
  }
}
