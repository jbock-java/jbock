package net.jbock.compiler.view;

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
